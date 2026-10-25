package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.request.ShopCreateRequestDto;
import az.shopery.core_ms.model.dto.response.ShopResponseDto;
import az.shopery.core_ms.model.dto.response.UserShopResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "shop-ms", url = "${feign.client.config.shop-ms.url}")
public interface ShopClient {

    @GetMapping("/api/v1/shops")
    ResponseEntity<SuccessResponse<Page<ShopResponseDto>>> getAllShops(Pageable pageable);

    @GetMapping("/api/v1/shops/name/{shopName}")
    ResponseEntity<SuccessResponse<ShopResponseDto>> getShopByShopName(@PathVariable String shopName);

    @GetMapping("/api/v1/shops/id/{shopId}")
    ResponseEntity<SuccessResponse<ShopResponseDto>> getShopById(@PathVariable String shopId);

    @PostMapping("/api/v1/users/me/shops")
    ResponseEntity<SuccessResponse<Void>> createMyShop(@RequestParam String email, @RequestBody ShopCreateRequestDto shopCreateRequestDto);

    @PostMapping("/api/v1/users/me/shops/{shopId}/rating")
    ResponseEntity<SuccessResponse<Void>> rateShop(@RequestParam String email, @PathVariable String shopId, @RequestParam int rating);

    @GetMapping("/api/v1/users/me/shops/dashboard")
    ResponseEntity<SuccessResponse<UserShopResponseDto>> getMyShop(@RequestParam String email);
}
