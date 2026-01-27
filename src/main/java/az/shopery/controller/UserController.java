package az.shopery.controller;

import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<SuccessResponseDto<UserProfileResponseDto>> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<SuccessResponseDto<UserProfileResponseDto>> updateMyProfile(Principal principal, @Valid @RequestBody UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
        return ResponseEntity.ok(userService.updateMyProfile(principal.getName(), userProfileUpdateRequestDto));
    }

    @PostMapping("/be-merchant")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<SuccessResponseDto<BecomeMerchantResponseDto>> becomeMerchant(Principal principal) {
        return ResponseEntity.ok(userService.becomeMerchant(principal.getName()));
    }

    @PostMapping("/shop")
    @PreAuthorize("hasAuthority('MERCHANT')")
    public ResponseEntity<SuccessResponseDto<Void>> createMyShop(Principal principal, @Valid @RequestBody ShopCreateRequestDto shopCreateRequestDto) {
        return ResponseEntity.ok(userService.createMyShop(principal.getName(), shopCreateRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PutMapping("/password")
    public ResponseEntity<SuccessResponseDto<UserPasswordUpdateResponseDto>> updatePassword(Principal principal, @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto){
        return ResponseEntity.ok(userService.updateMyPassword(principal.getName(), userPasswordUpdateRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PutMapping("/email")
    public ResponseEntity<SuccessResponseDto<Void>> changeMyEmail(Principal principal, @Valid @RequestBody UserEmailUpdateRequestDto userEmailUpdateRequestDto) {
        return ResponseEntity.ok(userService.changeMyEmail(principal.getName(), userEmailUpdateRequestDto));
    }

    @RateLimiter(name = "auth-rate-limiter")
    @PostMapping("/email/verify")
    public ResponseEntity<SuccessResponseDto<UserEmailUpdateResponseDto>> verifyMyEmail(Principal principal, @Valid @RequestBody UserEmailVerificationRequestDto userEmailVerificationRequestDto) {
        return ResponseEntity.ok(userService.verifyMyEmail(principal.getName(), userEmailVerificationRequestDto));
    }
}
