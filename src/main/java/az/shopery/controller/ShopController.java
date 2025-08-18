package az.shopery.controller;

import az.shopery.model.dto.response.ShopResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserShopResponseDto;
import az.shopery.service.ShopService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('MERCHANT')")
    public ResponseEntity<SuccessResponseDto<UserShopResponseDto>> getMyShop(Principal principal) {
        return ResponseEntity.ok(shopService.getMyShop(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDto<Page<ShopResponseDto>>> getAllOrSearchShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @GetMapping("/name/{shopName}")
    public ResponseEntity<SuccessResponseDto<ShopResponseDto>> getShopByShopName(@PathVariable String shopName) {
        return ResponseEntity.ok(shopService.getShopByShopName(shopName));
    }

    @GetMapping("/id/{shopId}")
    public ResponseEntity<SuccessResponseDto<ShopResponseDto>> getShopById(@PathVariable String shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }
}
