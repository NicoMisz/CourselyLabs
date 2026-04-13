package com.courselylabs.courselylab.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.PaymentDTO;
import com.courselylabs.courselylab.dto.SubscriptionDTO;
import com.courselylabs.courselylab.entity.PaymentEntity;
import com.courselylabs.courselylab.entity.SubscriptionEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.PaymentRepository;
import com.courselylabs.courselylab.repository.SubscriptionRepository;
import com.courselylabs.courselylab.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

@Service
@Transactional
public class StripeService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.monthly-price-id}")
    private String monthlyPriceId;

    @Value("${stripe.annual-price-id}")
    private String annualPriceId;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public StripeService(UserRepository userRepository,
                         SubscriptionRepository subscriptionRepository,
                         PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }

    public String createCheckoutSession(String email, String plan) throws StripeException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active")) {
            throw new BadRequestException("Ya tienes una suscripcion activa");
        }

        String priceId = "annual".equals(plan) ? annualPriceId : monthlyPriceId;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomerEmail(email)
                .setSuccessUrl(frontendUrl + "/pago/exito?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/pago/cancelado")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("userId", user.getId().toString())
                .putMetadata("plan", plan)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid webhook signature");
        } catch (Exception e) {
            throw new BadRequestException("Invalid webhook payload");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> { /* ignore other events */ }
        }
    }

    public SubscriptionDTO getSubscription(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(this::toSubscriptionDTO)
                .orElse(null);
    }

    public void cancelSubscription(String email) throws StripeException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        SubscriptionEntity sub = subscriptionRepository.findByUserIdAndStatus(user.getId(), "active")
                .orElseThrow(() -> new BadRequestException("No tienes una suscripcion activa"));

        // Cancel at period end via Stripe
        Subscription stripeSub = Subscription.retrieve(sub.getStripeSubscriptionId());
        stripeSub.update(Map.of("cancel_at_period_end", true));

        sub.setStatus("cancelled");
        sub.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentHistory(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toPaymentDTO)
                .toList();
    }

    public boolean isUserPremium(String email) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        if ("admin".equals(user.getRole())) return true;
        return subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active");
    }

    // --- Webhook handlers ---

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        // Idempotency
        if (paymentRepository.existsByStripeSessionId(session.getId())) return;

        String userId = session.getMetadata().get("userId");
        String plan = session.getMetadata().get("plan");

        UserEntity user = userRepository.findById(java.util.UUID.fromString(userId)).orElse(null);
        if (user == null) return;

        // Create subscription record
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setUser(user);
        sub.setStripeSubscriptionId(session.getSubscription());
        sub.setStripeCustomerId(session.getCustomer());
        sub.setPlan(plan);
        sub.setStatus("active");
        sub.setCurrentPeriodStart(LocalDateTime.now());
        sub.setCurrentPeriodEnd("annual".equals(plan)
                ? LocalDateTime.now().plusYears(1)
                : LocalDateTime.now().plusMonths(1));
        subscriptionRepository.save(sub);

        // Create payment record
        BigDecimal amount = "annual".equals(plan) ? new BigDecimal("60.00") : new BigDecimal("7.00");
        PaymentEntity payment = new PaymentEntity();
        payment.setUser(user);
        payment.setStripeSessionId(session.getId());
        payment.setType("subscription");
        payment.setDescription("Suscripcion Premium " + ("annual".equals(plan) ? "Anual" : "Mensual"));
        payment.setAmount(amount);
        payment.setCurrency("eur");
        payment.setStatus("completed");
        paymentRepository.save(payment);

        // Update user role to premium
        if (!"admin".equals(user.getRole())) {
            user.setRole("premium");
            userRepository.save(user);
        }
    }

    private void handleInvoicePaid(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice == null || invoice.getSubscription() == null) return;

        subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription())
                .ifPresent(sub -> {
                    Subscription stripeSub;
                    try {
                        stripeSub = Subscription.retrieve(invoice.getSubscription());
                    } catch (StripeException e) {
                        return;
                    }
                    sub.setStatus("active");
                    sub.setCurrentPeriodStart(fromEpoch(stripeSub.getCurrentPeriodStart()));
                    sub.setCurrentPeriodEnd(fromEpoch(stripeSub.getCurrentPeriodEnd()));
                    subscriptionRepository.save(sub);
                });
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription stripeSub = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(sub -> {
                    sub.setStatus(mapStripeStatus(stripeSub.getStatus()));
                    sub.setCurrentPeriodStart(fromEpoch(stripeSub.getCurrentPeriodStart()));
                    sub.setCurrentPeriodEnd(fromEpoch(stripeSub.getCurrentPeriodEnd()));
                    if (stripeSub.getCancelAtPeriodEnd()) {
                        sub.setStatus("cancelled");
                    }
                    subscriptionRepository.save(sub);
                });
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription stripeSub = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(sub -> {
                    sub.setStatus("expired");
                    subscriptionRepository.save(sub);

                    // Revert user role to 'user' if no other active subscriptions
                    UserEntity user = sub.getUser();
                    if (!subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active")
                            && !"admin".equals(user.getRole())) {
                        user.setRole("user");
                        userRepository.save(user);
                    }
                });
    }

    // --- Helpers ---

    private String mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> "active";
            case "past_due" -> "past_due";
            case "canceled" -> "expired";
            default -> stripeStatus;
        };
    }

    private LocalDateTime fromEpoch(Long epoch) {
        if (epoch == null) return null;
        return Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private SubscriptionDTO toSubscriptionDTO(SubscriptionEntity e) {
        return new SubscriptionDTO(
                e.getId().toString(),
                e.getPlan(),
                e.getStatus(),
                e.getCurrentPeriodStart(),
                e.getCurrentPeriodEnd(),
                e.getCancelledAt(),
                e.getCreatedAt()
        );
    }

    private PaymentDTO toPaymentDTO(PaymentEntity e) {
        return new PaymentDTO(
                e.getId().toString(),
                e.getType(),
                e.getDescription(),
                e.getAmount(),
                e.getCurrency(),
                e.getStatus(),
                e.getCreatedAt()
        );
    }
}
