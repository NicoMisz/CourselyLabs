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

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutCompleted(event);
                case "invoice.paid" -> handleInvoicePaid(event);
                case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
                case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
                default -> { /* ignore other events */ }
            }
        } catch (Exception e) {
            System.err.println("[Stripe Webhook] ERROR processing " + event.getType() + ": " + e.getMessage());
            e.printStackTrace();
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

    public java.util.Map<String, Object> getPricingInfo() throws StripeException {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("monthly", fetchPriceInfo(monthlyPriceId));
        result.put("annual", fetchPriceInfo(annualPriceId));
        return result;
    }

    private java.util.Map<String, Object> fetchPriceInfo(String priceId) {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        if (priceId == null || priceId.isBlank()) {
            info.put("amount", null);
            info.put("currency", "eur");
            return info;
        }
        try {
            var price = com.stripe.model.Price.retrieve(priceId);
            info.put("amount", price.getUnitAmount() != null
                    ? new BigDecimal(price.getUnitAmount()).movePointLeft(2)
                    : null);
            info.put("currency", price.getCurrency());
        } catch (StripeException e) {
            info.put("amount", null);
            info.put("currency", "eur");
        }
        return info;
    }

    public boolean isUserPremium(String email) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        if ("admin".equals(user.getRole())) return true;
        return subscriptionRepository.existsByUserIdAndStatus(user.getId(), "active");
    }

    /**
     * Confirms a checkout session from the frontend after successful payment redirect.
     * This is the primary way to activate subscriptions — does not depend on webhooks.
     */
    public SubscriptionDTO confirmCheckoutSession(String email, String sessionId) throws StripeException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Session session = Session.retrieve(sessionId);

        if (!"paid".equals(session.getPaymentStatus()) && !"complete".equals(session.getStatus())) {
            throw new BadRequestException("El pago no esta completado");
        }

        String plan = session.getMetadata() != null && session.getMetadata().get("plan") != null
                ? session.getMetadata().get("plan")
                : "monthly";

        activateSubscription(user, session, plan);

        return subscriptionRepository.findByUserIdAndStatus(user.getId(), "active")
                .map(s -> new SubscriptionDTO(
                        s.getId().toString(), s.getPlan(), s.getStatus(),
                        s.getCurrentPeriodStart(), s.getCurrentPeriodEnd(),
                        s.getCancelledAt(), s.getCreatedAt()))
                .orElseThrow(() -> new BadRequestException("No se pudo activar la suscripcion"));
    }

    private void activateSubscription(UserEntity user, Session session, String plan) {
        // Idempotency — don't duplicate
        if (paymentRepository.existsByStripeSessionId(session.getId())) return;

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

        // Get actual amount and currency from Stripe session (cents -> euros)
        BigDecimal amount;
        if (session.getAmountTotal() != null) {
            amount = new BigDecimal(session.getAmountTotal()).movePointLeft(2);
        } else {
            amount = "annual".equals(plan) ? new BigDecimal("60.00") : new BigDecimal("7.00");
        }
        String currency = session.getCurrency() != null ? session.getCurrency() : "eur";

        PaymentEntity payment = new PaymentEntity();
        payment.setUser(user);
        payment.setStripeSessionId(session.getId());
        payment.setType("subscription");
        payment.setDescription("Suscripcion Premium " + ("annual".equals(plan) ? "Anual" : "Mensual"));
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus("completed");
        paymentRepository.save(payment);

        if (!"admin".equals(user.getRole())) {
            user.setRole("premium");
            userRepository.save(user);
        }
    }

    // --- Webhook handlers ---

    private void handleCheckoutCompleted(Event event) {
        Session session;
        // Try direct deserialization first, fallback to fetching from Stripe API
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            session = (Session) deserializer.getObject().get();
        } else {
            // SDK/API version mismatch — fetch session from Stripe API using raw JSON id
            try {
                String rawJson = deserializer.getRawJson();
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String sessionId = mapper.readTree(rawJson).get("id").asText();
                session = Session.retrieve(sessionId);
                System.out.println("[Stripe Webhook] Fetched session from API: " + sessionId);
            } catch (Exception e) {
                System.err.println("[Stripe Webhook] checkout.session.completed: could not deserialize or fetch session: " + e.getMessage());
                return;
            }
        }

        // Idempotency
        if (paymentRepository.existsByStripeSessionId(session.getId())) return;

        // Try to get user from metadata first, then from customer_email
        UserEntity user = null;
        String plan = "monthly"; // default

        if (session.getMetadata() != null && session.getMetadata().get("userId") != null) {
            try {
                user = userRepository.findById(java.util.UUID.fromString(session.getMetadata().get("userId"))).orElse(null);
                plan = session.getMetadata().getOrDefault("plan", "monthly");
            } catch (Exception e) {
                System.err.println("[Stripe Webhook] Error parsing metadata userId: " + e.getMessage());
            }
        }

        // Fallback: find user by customer email
        if (user == null && session.getCustomerEmail() != null) {
            user = userRepository.findByEmail(session.getCustomerEmail()).orElse(null);
        }

        // Fallback: retrieve session from Stripe API to get customer email
        if (user == null && session.getCustomer() != null) {
            try {
                var customer = com.stripe.model.Customer.retrieve(session.getCustomer());
                if (customer.getEmail() != null) {
                    user = userRepository.findByEmail(customer.getEmail()).orElse(null);
                }
            } catch (StripeException e) {
                System.err.println("[Stripe Webhook] Error retrieving customer: " + e.getMessage());
            }
        }

        if (user == null) {
            System.err.println("[Stripe Webhook] checkout.session.completed: could not find user. session=" + session.getId()
                + " customerEmail=" + session.getCustomerEmail() + " customer=" + session.getCustomer()
                + " metadata=" + session.getMetadata());
            return;
        }

        System.out.println("[Stripe Webhook] checkout.session.completed: user=" + user.getEmail() + " plan=" + plan);

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
        // Get actual amount and currency from Stripe session (cents -> euros)
        BigDecimal amount;
        if (session.getAmountTotal() != null) {
            amount = new BigDecimal(session.getAmountTotal()).movePointLeft(2);
        } else {
            amount = "annual".equals(plan) ? new BigDecimal("60.00") : new BigDecimal("7.00");
        }
        String currency = session.getCurrency() != null ? session.getCurrency() : "eur";

        PaymentEntity payment = new PaymentEntity();
        payment.setUser(user);
        payment.setStripeSessionId(session.getId());
        payment.setType("subscription");
        payment.setDescription("Suscripcion Premium " + ("annual".equals(plan) ? "Anual" : "Mensual"));
        payment.setAmount(amount);
        payment.setCurrency(currency);
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
