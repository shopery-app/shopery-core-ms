package az.shopery.controller;

import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserShopResponseDto;
import az.shopery.service.ShopRatingService;
import az.shopery.service.ShopService;
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

    private final ShopService shopService;
    private final ShopRatingService shopRatingService;

    @PostMapping("/{shopId}/rating")
    public ResponseEntity<SuccessResponse<Void>> rateShop(Principal principal, @PathVariable String shopId, @RequestParam int rating) {
        return ResponseEntity.ok(shopRatingService.rateShop(principal.getName(), shopId, rating));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<UserShopResponseDto>> getMyShop(Principal principal) {
        return ResponseEntity.ok(shopService.getMyShop(principal.getName()));
    }
}
