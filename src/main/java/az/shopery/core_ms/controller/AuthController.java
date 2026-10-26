package az.shopery.core_ms.controller;

import az.shopery.core_ms.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.core_ms.model.dto.request.RefreshTokenRequestDto;
import az.shopery.core_ms.model.dto.request.ResendCodeRequestDto;
import az.shopery.core_ms.model.dto.request.ResetPasswordRequestDto;
import az.shopery.core_ms.model.dto.request.UserLoginRequestDto;
import az.shopery.core_ms.model.dto.request.UserRegisterRequestDto;
import az.shopery.core_ms.model.dto.request.UserVerificationRequestDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.UserAuthResponseDto;
import az.shopery.core_ms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<Void>> register(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return ResponseEntity.ok(authService.register(userRegisterRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return ResponseEntity.ok(authService.login(userLoginRequestDto));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>>  adminLogin(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return ResponseEntity.ok(authService.adminLogin(userLoginRequestDto));
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> verifyAccount(@Valid @RequestBody UserVerificationRequestDto userVerificationRequestDto) {
        return ResponseEntity.ok(authService.verifyAccount(userVerificationRequestDto));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<SuccessResponse<Void>> resendCode(@Valid @RequestBody ResendCodeRequestDto resendCodeRequestDto) {
        return ResponseEntity.ok(authService.resendVerificationCode(resendCodeRequestDto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
        return ResponseEntity.ok(authService.forgotPassword(forgotPasswordRequestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        return ResponseEntity.ok(authService.resetPassword(resetPasswordRequestDto));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequestDto));
    }
}
