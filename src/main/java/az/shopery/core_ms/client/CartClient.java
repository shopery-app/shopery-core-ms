package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.request.CartItemUpdateRequestDto;
import az.shopery.core_ms.model.dto.response.CartResponseDto;
import az.shopery.core_ms.model.dto.response.WishlistResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cart-ms", url = "${feign.client.config.cart-ms.url}")
public interface CartClient {

    @GetMapping("/api/v1/users/me/cart")
    ResponseEntity<SuccessResponse<CartResponseDto>> getMyCart(@RequestParam String email);

    @PostMapping("/api/v1/users/me/cart/{productId}")
    ResponseEntity<SuccessResponse<CartResponseDto>> addProductToCart(@RequestParam String email, @PathVariable String productId, @RequestBody CartItemUpdateRequestDto cartItemUpdateRequestDto);

    @PutMapping("/api/v1/users/me/cart/{productId}")
    ResponseEntity<SuccessResponse<CartResponseDto>> updateProductInCart(@RequestParam String email, @PathVariable String productId, @RequestBody CartItemUpdateRequestDto cartItemUpdateRequestDto);

    @DeleteMapping("/api/v1/users/me/cart/{productId}")
    ResponseEntity<SuccessResponse<CartResponseDto>> removeProductFromCart(@RequestParam String email, @PathVariable String productId);

    @DeleteMapping("/api/v1/users/me/cart")
    ResponseEntity<SuccessResponse<CartResponseDto>> removeAllProductsFromCart(@RequestParam String email);

    @PostMapping("/api/v1/users/me/cart/move-from-wishlist/{productId}")
    ResponseEntity<SuccessResponse<CartResponseDto>> moveFromWishlist(@RequestParam String email, @PathVariable String productId);

    @GetMapping("/api/v1/users/me/wishlist")
    ResponseEntity<SuccessResponse<WishlistResponseDto>> getMyWishlist(@RequestParam String email);

    @PostMapping("/api/v1/users/me/wishlist/{productId}")
    ResponseEntity<SuccessResponse<WishlistResponseDto>> addProductToWishlist(@RequestParam String email, @PathVariable String productId);

    @DeleteMapping("/api/v1/users/me/wishlist/{productId}")
    ResponseEntity<SuccessResponse<WishlistResponseDto>> removeProductFromWishlist(@RequestParam String email, @PathVariable String productId);

    @DeleteMapping("/api/v1/users/me/wishlist")
    ResponseEntity<SuccessResponse<WishlistResponseDto>> removeAllProductsFromWishlist(@RequestParam String email);
}
