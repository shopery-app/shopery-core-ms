package az.shopery.service;

import az.shopery.model.dto.response.CartResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;

public interface CartService {
    SuccessResponse<CartResponseDto> getMyCart(String userEmail);
    SuccessResponse<CartResponseDto> addProductToCart(String userEmail, String productId, int quantity);
    SuccessResponse<CartResponseDto> updateProductQuantity(String userEmail, String productId, int quantity);
    SuccessResponse<CartResponseDto> removeProductFromCart(String userEmail, String productId);
    SuccessResponse<CartResponseDto> removeAllProductsFromCart(String userEmail);
    SuccessResponse<CartResponseDto> moveProductFromWishlistToCart(String userEmail, String productId);
}
