package az.shopery.controller;

import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponseDto<Void>> register(
            @Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return ResponseEntity.ok(authService.register(userRegisterRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDto<UserAuthResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return ResponseEntity.ok(authService.login(userLoginRequestDto));
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessResponseDto<UserAuthResponseDto>> verifyAccount(
            @Valid @RequestBody UserVerificationRequestDto userVerificationRequestDto) {
        return ResponseEntity.ok(authService.verifyAccount(userVerificationRequestDto));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<SuccessResponseDto<Void>> resendCode(
            @Valid @RequestBody ResendCodeRequestDto resendCodeRequestDto) {
        return ResponseEntity.ok(authService.resendVerificationCode(resendCodeRequestDto));
    }
}
