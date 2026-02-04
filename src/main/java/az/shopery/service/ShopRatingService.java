package az.shopery.service;

import az.shopery.model.dto.shared.SuccessResponse;

public interface ShopRatingService {
    SuccessResponse<Void> rateShop(String userEmail, String shopId, int ratingValue);
}
