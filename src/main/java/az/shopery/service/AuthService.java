package az.shopery.service;

import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;

public interface AuthService {
    SuccessResponseDto<Void> register(UserRegisterRequestDto userRegisterRequestDto);
    SuccessResponseDto<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto);
    SuccessResponseDto<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto);
    SuccessResponseDto<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto);
}
