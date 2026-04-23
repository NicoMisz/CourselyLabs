package com.courselylabs.courselylab.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courselylabs.courselylab.dto.PaymentDTO;
import com.courselylabs.courselylab.dto.SubscriptionDTO;
import com.courselylabs.courselylab.service.StripeService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final StripeService stripeService;

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            @RequestBody Map<String, String> body,
            Authentication auth) throws StripeException {
        String plan = body.getOrDefault("plan", "monthly");
        String url = stripeService.createCheckoutSession(auth.getName(), plan);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/confirm")
    public ResponseEntity<SubscriptionDTO> confirmPayment(
            @RequestBody Map<String, String> body,
            Authentication auth) throws StripeException {
        String sessionId = body.get("sessionId");
        return ResponseEntity.ok(stripeService.confirmCheckoutSession(auth.getName(), sessionId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionDTO> getSubscription(Authentication auth) {
        SubscriptionDTO sub = stripeService.getSubscription(auth.getName());
        return ResponseEntity.ok(sub);
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<Void> cancelSubscription(Authentication auth) throws StripeException {
        stripeService.cancelSubscription(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentDTO>> getHistory(Authentication auth) {
        return ResponseEntity.ok(stripeService.getPaymentHistory(auth.getName()));
    }

    @GetMapping("/is-premium")
    public ResponseEntity<Map<String, Boolean>> isPremium(Authentication auth) {
        return ResponseEntity.ok(Map.of("premium", stripeService.isUserPremium(auth.getName())));
    }

    @GetMapping("/pricing")
    public ResponseEntity<Map<String, Object>> getPricing() throws StripeException {
        return ResponseEntity.ok(stripeService.getPricingInfo());
    }
}
