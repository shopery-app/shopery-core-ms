package az.shopery.core_ms.controller;

import az.shopery.core_ms.client.ShopClient;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.UserShopResponseDto;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopClient shopClient;

    @PostMapping("/{shopId}/rating")
    public ResponseEntity<SuccessResponse<Void>> rateShop(Principal principal, @PathVariable String shopId, @RequestParam int rating) {
        return shopClient.rateShop(principal.getName(), shopId, rating);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<UserShopResponseDto>> getMyShop(Principal principal) {
        return shopClient.getMyShop(principal.getName());
    }
}
