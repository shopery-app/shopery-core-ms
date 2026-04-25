package az.shopery.controller;

import az.shopery.model.dto.response.StripeCheckoutResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.PaymentService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/stripe/checkout")
    public ResponseEntity<SuccessResponse<StripeCheckoutResponseDto>> createCheckoutSession(Principal principal) {
        return ResponseEntity.ok(paymentService.createCheckoutSession(principal.getName()));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<SuccessResponse<Void>> stripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader) {
        return ResponseEntity.ok(paymentService.handleStripeWebhook(payload, signatureHeader));
    }
}
