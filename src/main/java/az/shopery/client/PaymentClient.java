package az.shopery.client;

import az.shopery.model.dto.response.StripeCheckoutResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-ms", url = "${feign.client.config.payment-ms.url}")
public interface PaymentClient {

    @PostMapping("/api/v1/payments/stripe/checkout")
    ResponseEntity<SuccessResponse<StripeCheckoutResponseDto>> createCheckoutSession(@RequestParam String email);

    @PostMapping("api/v1/stripe/webhook")
    ResponseEntity<SuccessResponse<Void>> stripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader);
}
