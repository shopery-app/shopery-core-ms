package az.shopery.service;

import az.shopery.model.dto.response.SuccessResponseDto;

public interface ShopRatingService {
    SuccessResponseDto<Void> rateShop(String userEmail, String shopId, int ratingValue);
}
