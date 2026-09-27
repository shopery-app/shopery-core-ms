package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.WishlistResponseDto;

public interface WishlistService {
    SuccessResponse<WishlistResponseDto> getMyWishlist(String userEmail);
    SuccessResponse<WishlistResponseDto> addProductToWishlist(String userEmail, String productId);
    SuccessResponse<WishlistResponseDto> removeProductFromWishlist(String userEmail, String productId);
    SuccessResponse<WishlistResponseDto> removeAllProductsFromWishlist(String userEmail);
}
