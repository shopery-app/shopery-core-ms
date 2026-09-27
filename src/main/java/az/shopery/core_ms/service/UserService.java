package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.request.ShopCreateRequestDto;
import az.shopery.core_ms.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.core_ms.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.core_ms.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.core_ms.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.core_ms.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.core_ms.model.dto.response.UserProfileResponseDto;

public interface UserService {
    SuccessResponse<UserProfileResponseDto> getMyProfile(String userEmail);
    SuccessResponse<UserProfileResponseDto> updateMyProfile(String userEmail, UserProfileUpdateRequestDto dto);
    SuccessResponse<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto);
    SuccessResponse<UserPasswordUpdateResponseDto> updateMyPassword(String userEmail, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto);
    SuccessResponse<Void> changeMyEmail(String userEmail, UserEmailUpdateRequestDto userEmailUpdateRequestDto);
    SuccessResponse<UserEmailUpdateResponseDto> verifyMyEmail(String userEmail, UserEmailVerificationRequestDto userEmailVerificationRequestDto);
}
