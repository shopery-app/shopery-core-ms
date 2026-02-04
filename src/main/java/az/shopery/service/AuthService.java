package az.shopery.service;

import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.RefreshTokenRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserAuthResponseDto;

public interface AuthService {
    SuccessResponse<Void> register(UserRegisterRequestDto userRegisterRequestDto);
    SuccessResponse<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto);
    SuccessResponse<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto);
    SuccessResponse<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto);
    SuccessResponse<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto);
    SuccessResponse<Void> resetPassword(ResetPasswordRequestDto resetPasswordRequestDto);
    SuccessResponse<UserAuthResponseDto> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
}
