package az.shopery.core_ms.controller;

import az.shopery.core_ms.client.CartClient;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.WishlistResponseDto;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final CartClient cartClient;

    @GetMapping
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> getMyWishlist(Principal principal) {
        return cartClient.getMyWishlist(principal.getName());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> addProductToWishlist(Principal principal, @PathVariable String productId) {
        return cartClient.addProductToWishlist(principal.getName(), productId);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> removeProductFromWishlist(Principal principal, @PathVariable String productId) {
        return cartClient.removeProductFromWishlist(principal.getName(), productId);
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> removeAllProductsFromWishlist(Principal principal) {
        return cartClient.removeAllProductsFromWishlist(principal.getName());
    }
}
