package az.shopery.service;

import az.shopery.model.dto.response.ShopResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserShopResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopService {
    SuccessResponse<UserShopResponseDto> getMyShop(String userEmail);
    SuccessResponse<Page<ShopResponseDto>> getAllShops(Pageable pageable);
    SuccessResponse<ShopResponseDto> getShopById(String shopId);
    SuccessResponse<ShopResponseDto> getShopByShopName(String shopName);
}
