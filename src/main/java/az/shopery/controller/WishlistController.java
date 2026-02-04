package az.shopery.controller;

import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.WishlistResponseDto;
import az.shopery.service.WishlistService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> getMyWishlist(Principal principal) {
        return ResponseEntity.ok(wishlistService.getMyWishlist(principal.getName()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> addProductToWishlist(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(principal.getName(), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> removeProductFromWishlist(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.removeProductFromWishlist(principal.getName(), productId));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<WishlistResponseDto>> removeAllProductsFromWishlist(Principal principal) {
        return ResponseEntity.ok(wishlistService.removeAllProductsFromWishlist(principal.getName()));
    }
}
