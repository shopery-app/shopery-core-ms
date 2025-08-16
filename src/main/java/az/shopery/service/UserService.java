package az.shopery.service;

import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;

public interface UserService {
    SuccessResponseDto<UserProfileResponseDto> getMyProfile(String userEmail);
    SuccessResponseDto<UserProfileResponseDto> updateMyProfile(String userEmail, UserProfileUpdateRequestDto dto);
    SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail);
    SuccessResponseDto<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto);
}
