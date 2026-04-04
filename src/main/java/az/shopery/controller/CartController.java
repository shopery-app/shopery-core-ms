package az.shopery.controller;

import az.shopery.model.dto.request.CartItemUpdateRequestDto;
import az.shopery.model.dto.response.CartResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.CartService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<SuccessResponse<CartResponseDto>> getMyCart(Principal principal) {
        return ResponseEntity.ok(cartService.getMyCart(principal.getName()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> addProductToCart(Principal principal, @PathVariable String productId, @RequestBody @Valid CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        return ResponseEntity.ok(cartService.addProductToCart(principal.getName(), productId, cartItemUpdateRequestDto.getQuantity()));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> updateProductInCart(Principal principal, @PathVariable String productId, @RequestBody @Valid CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        return ResponseEntity.ok(cartService.updateProductQuantity(principal.getName(), productId, cartItemUpdateRequestDto.getQuantity()));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> removeProductFromCart(Principal principal,  @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(principal.getName(), productId));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<CartResponseDto>> removeAllProductsFromCart(Principal principal) {
        return ResponseEntity.ok(cartService.removeAllProductsFromCart(principal.getName()));
    }

    @PostMapping("/move-from-wishlist/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> moveFromWishlist(Principal principal, @PathVariable String productId) {
        return ResponseEntity.ok(cartService.moveProductFromWishlistToCart(principal.getName(), productId));
    }
}
