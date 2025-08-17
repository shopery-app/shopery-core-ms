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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<SuccessResponseDto<Page<ShopResponseDto>>> getAllShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDto<ShopResponseDto>> getShopById(@RequestParam String id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDto<ShopResponseDto>> getShopByShopName(@RequestParam String name) {
        return ResponseEntity.ok(shopService.getShopByShopName(name));
    }
}
