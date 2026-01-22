package az.shopery.service;

import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    SuccessResponseDto<UserProfileResponseDto> getMyProfile(String userEmail);
    SuccessResponseDto<UserProfileResponseDto> updateMyProfile(String userEmail, UserProfileUpdateRequestDto dto);
    SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail);
    SuccessResponseDto<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto);
    SuccessResponseDto<UserPasswordUpdateResponseDto> updateMyPassword(String userEmail, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto);
    SuccessResponseDto<Void> changeMyEmail(String userEmail, UserEmailUpdateRequestDto userEmailUpdateRequestDto);
    SuccessResponseDto<UserEmailUpdateResponseDto> verifyMyEmail(String userEmail, UserEmailVerificationRequestDto userEmailVerificationRequestDto);
    SuccessResponseDto<Void> saveBlog(String userEmail, String blogId);
    SuccessResponseDto<Page<BlogResponseDto>> getSavedBlogs(String userEmail, Pageable pageable);
    SuccessResponseDto<Void> deleteSavedBlog(String userEmail, String blogId);
}
