package az.shopery.core_ms.controller;

import az.shopery.core_ms.client.CartClient;
import az.shopery.core_ms.model.dto.request.CartItemUpdateRequestDto;
import az.shopery.core_ms.model.dto.response.CartResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
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

    private final CartClient cartClient;

    @GetMapping
    public ResponseEntity<SuccessResponse<CartResponseDto>> getMyCart(Principal principal) {
        return cartClient.getMyCart(principal.getName());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> addProductToCart(Principal principal, @PathVariable String productId, @RequestBody @Valid CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        return cartClient.addProductToCart(principal.getName(), productId, cartItemUpdateRequestDto);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> updateProductInCart(Principal principal, @PathVariable String productId, @RequestBody @Valid CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        return cartClient.updateProductInCart(principal.getName(), productId, cartItemUpdateRequestDto);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> removeProductFromCart(Principal principal,  @PathVariable String productId) {
        return cartClient.removeProductFromCart(principal.getName(), productId);
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponse<CartResponseDto>> removeAllProductsFromCart(Principal principal) {
        return cartClient.removeAllProductsFromCart(principal.getName());
    }

    @PostMapping("/move-from-wishlist/{productId}")
    public ResponseEntity<SuccessResponse<CartResponseDto>> moveFromWishlist(Principal principal, @PathVariable String productId) {
        return cartClient.moveFromWishlist(principal.getName(), productId);
    }
}
