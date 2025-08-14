package az.shopery.service;

import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.RefreshTokenRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserAuthResponseDto;

public interface AuthService {
    SuccessResponseDto<Void> register(UserRegisterRequestDto userRegisterRequestDto);
    SuccessResponseDto<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto);
    SuccessResponseDto<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto);
    SuccessResponseDto<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto);
    SuccessResponseDto<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto);
    SuccessResponseDto<Void> resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);
    SuccessResponseDto<UserAuthResponseDto> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
}
