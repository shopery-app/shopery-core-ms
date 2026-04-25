package az.shopery.service;

import az.shopery.model.dto.response.StripeCheckoutResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;

public interface PaymentService {
    SuccessResponse<StripeCheckoutResponseDto> createCheckoutSession(String userEmail);
    SuccessResponse<Void> handleStripeWebhook(String payload, String signatureHeader);
}
