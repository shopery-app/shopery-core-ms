package az.shopery.controller;

import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserShopResponseDto;
import az.shopery.service.ShopService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/shops")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MERCHANT')")
public class MerchantShopController {

    private final ShopService shopService;

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<UserShopResponseDto>> getMyShop(Principal principal) {
        return ResponseEntity.ok(shopService.getMyShop(principal.getName()));
    }
}
