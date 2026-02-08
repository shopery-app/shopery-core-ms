package az.shopery.controller;

import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.RefreshTokenRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<Void>> register(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return ResponseEntity.ok(authService.register(userRegisterRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return ResponseEntity.ok(authService.login(userLoginRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/admin/login")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>>  adminLogin(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return ResponseEntity.ok(authService.adminLogin(userLoginRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/verify")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> verifyAccount(@Valid @RequestBody UserVerificationRequestDto userVerificationRequestDto) {
        return ResponseEntity.ok(authService.verifyAccount(userVerificationRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/resend-code")
    public ResponseEntity<SuccessResponse<Void>> resendCode(@Valid @RequestBody ResendCodeRequestDto resendCodeRequestDto) {
        return ResponseEntity.ok(authService.resendVerificationCode(resendCodeRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
        return ResponseEntity.ok(authService.forgotPassword(forgotPasswordRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        return ResponseEntity.ok(authService.resetPassword(resetPasswordRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<UserAuthResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequestDto));
    }
}
