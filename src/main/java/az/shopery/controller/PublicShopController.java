package az.shopery.controller;

import az.shopery.model.dto.response.ShopResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class PublicShopController {

    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<ShopResponseDto>>> getAllOrSearchShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @GetMapping("/name/{shopName}")
    public ResponseEntity<SuccessResponse<ShopResponseDto>> getShopByShopName(@PathVariable String shopName) {
        return ResponseEntity.ok(shopService.getShopByShopName(shopName));
    }

    @GetMapping("/id/{shopId}")
    public ResponseEntity<SuccessResponse<ShopResponseDto>> getShopById(@PathVariable String shopId) {
        return ResponseEntity.ok(shopService.getShopById(shopId));
    }
}
