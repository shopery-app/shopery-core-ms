package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.shared.SuccessResponse;

public interface ShopRatingService {
    SuccessResponse<Void> rateShop(String userEmail, String shopId, int ratingValue);
}
