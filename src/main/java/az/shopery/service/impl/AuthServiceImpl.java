package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.COOLDOWN_SECONDS;
import static az.shopery.utils.common.CommonConstraints.LOCK_DURATION_MINUTES;
import static az.shopery.utils.common.CommonConstraints.MAX_FAILED_ATTEMPTS;
import static az.shopery.utils.common.CommonConstraints.RESET_TOKEN_EXPIRY_MINUTES;
import static az.shopery.utils.common.CommonConstraints.VERIFICATION_CODE_EXPIRY_MINUTES;
import static az.shopery.utils.common.VerificationCodeGenerator.generateSixDigitVerificationCode;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.shopery.handler.exception.CooldownNotMetException;
import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.redis.CachedPasswordResetData;
import az.shopery.model.dto.redis.CachedVerificationData;
import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.RefreshTokenRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.UserRepository;
import az.shopery.service.AuthService;
import az.shopery.service.RedisService;
import az.shopery.utils.common.RedisUtils;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import az.shopery.utils.enums.VerificationProgress;
import az.shopery.utils.security.JwtService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisService redisService;

    @Override
    @Transactional
    public SuccessResponse<Void> register(UserRegisterRequestDto userRegisterRequestDto) {
        String email = userRegisterRequestDto.getEmail();
        if (userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE).isPresent()) {
            throw new EmailAlreadyExistsException("Email '" + email + "' is already in use.");
        }

        String code = generateSixDigitVerificationCode();

        CachedVerificationData cachedVerificationData = CachedVerificationData.builder()
                .hashedToken(passwordEncoder.encode(code))
                .userName(userRegisterRequestDto.getName())
                .userEmail(email)
                .hashedPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()))
                .attemptCount(0)
                .progress(VerificationProgress.PENDING)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .codeLastSentAt(LocalDateTime.now())
                .build();

        redisService.set(
                RedisUtils.registerKey(email),
                cachedVerificationData,
                Duration.ofMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)
        );

        applicationEventPublisher.publishEvent(new NotificationEvent(
                email,
                NotificationType.VERIFICATION_CODE,
                Map.of(
                        "userName", userRegisterRequestDto.getName(),
                        "isRegistration", Boolean.TRUE,
                        "verificationCode", code
                )
        ));
        return SuccessResponse.of("Verification code sent to your email. Please verify to complete registration.");
    }


    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponse<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto) {
        String email = verificationRequestDto.getEmail();
        CachedVerificationData cachedVerificationData = redisService
                .get(RedisUtils.registerKey(email), CachedVerificationData.class)
                .filter(data -> data.getProgress().equals(VerificationProgress.PENDING))
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found. It may have expired or been verified already."));

        if (cachedVerificationData.getExpiryDate().isBefore(LocalDateTime.now())) {
            redisService.delete(RedisUtils.registerKey(email));
            throw new InvalidCredentialsException("Verification token expired. Please request a new one!");
        }

        if (!passwordEncoder.matches(verificationRequestDto.getCode(), cachedVerificationData.getHashedToken())) {
            cachedVerificationData.setAttemptCount(cachedVerificationData.getAttemptCount() + 1);
            if (cachedVerificationData.getAttemptCount() >= MAX_FAILED_ATTEMPTS) {
                redisService.delete(RedisUtils.registerKey(email));
                throw new InvalidCredentialsException("Invalid verification code. You have exceeded the maximum number of attempts.");
            }

            long remaining = Duration.between(LocalDateTime.now(), cachedVerificationData.getExpiryDate()).toMinutes();
            redisService.set(
                    RedisUtils.registerKey(email),
                    cachedVerificationData,
                    Duration.ofMinutes(Math.max(remaining, 1))
            );

            throw new InvalidCredentialsException("Invalid verification code!");
        }

        var user = UserEntity.builder()
                .name(cachedVerificationData.getUserName())
                .email(cachedVerificationData.getUserEmail())
                .password(cachedVerificationData.getHashedPassword())
                .build();
        userRepository.save(user);

        redisService.delete(RedisUtils.registerKey(email));

        return SuccessResponse.of(generateAuthResponse(user), "Account verified successfully. Welcome to Shopery!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto) {
        String email = resendCodeRequestDto.getEmail();
        if (userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE).isPresent()) {
            throw new EmailAlreadyExistsException("This account has already been verified.");
        }

        CachedVerificationData existing = redisService
                .get(RedisUtils.registerKey(email), CachedVerificationData.class)
                .orElseThrow(() -> new ResourceNotFoundException("No registration process found for this email! Please register first!"));

        String newCode = generateSixDigitVerificationCode();

        boolean cooldownSet = redisService.setIfAbsent(
                RedisUtils.registerCooldownKey(email),
                "1",
                Duration.ofSeconds(COOLDOWN_SECONDS)
        );
        if (!cooldownSet) {
            throw new CooldownNotMetException("Please wait before resending the verification code!");
        }

        CachedVerificationData cachedVerificationData = CachedVerificationData.builder()
                .hashedToken(passwordEncoder.encode(newCode))
                .userName(existing.getUserName())
                .userEmail(email)
                .hashedPassword(existing.getHashedPassword())
                .attemptCount(0)
                .progress(VerificationProgress.PENDING)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .codeLastSentAt(LocalDateTime.now())
                .build();

        redisService.set(
                RedisUtils.registerKey(email),
                cachedVerificationData,
                Duration.ofMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)
        );

        applicationEventPublisher.publishEvent(new NotificationEvent(
                email,
                NotificationType.VERIFICATION_CODE,
                Map.of(
                        "userName", existing.getUserName(),
                        "isRegistration", Boolean.TRUE,
                        "verificationCode", newCode
                )
        ));
        return SuccessResponse.of("A new verification code has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        String email = forgotPasswordRequestDto.getEmail();
        var userEntity = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean cooldownSet = redisService.setIfAbsent(
                RedisUtils.resetCooldownKey(email),
                "1",
                Duration.ofSeconds(COOLDOWN_SECONDS)
        );
        if (!cooldownSet) {
            throw new CooldownNotMetException("Please wait before requesting a new password reset link!");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES);

        CachedPasswordResetData cachedPasswordResetData = CachedPasswordResetData.builder()
                .token(token)
                .userEmail(email)
                .expiryDate(expiry)
                .linkLastSentAt(LocalDateTime.now())
                .build();

        redisService.set(
                RedisUtils.resetTokenKey(token),
                cachedPasswordResetData,
                Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES)
        );

        applicationEventPublisher.publishEvent(new NotificationEvent(
                email,
                NotificationType.PASSWORD_RESET_LINK,
                Map.of(
                        "userName", userEntity.getName(),
                        "token", token
                )
        ));
        return SuccessResponse.of("A new password reset link has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        String token = resetPasswordRequestDto.getToken();
        CachedPasswordResetData cachedPasswordResetData = redisService
                .get(RedisUtils.resetTokenKey(token), CachedPasswordResetData.class)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired password reset token!"));

        if (cachedPasswordResetData.getExpiryDate().isBefore(LocalDateTime.now())) {
            redisService.delete(RedisUtils.resetTokenKey(token));
            throw new InvalidCredentialsException("Password reset token expired! Please request a new one!");
        }

        String email = cachedPasswordResetData.getUserEmail();

        var user = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for the given token."));

        user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
        userRepository.save(user);

        redisService.delete(RedisUtils.resetTokenKey(token));
        redisService.delete(RedisUtils.resetCooldownKey(email));

        applicationEventPublisher.publishEvent(new NotificationEvent(
                email,
                NotificationType.PASSWORD_CHANGED,
                Map.of(
                        "userName", user.getName()
                )
        ));
        return SuccessResponse.of("Password reset successful.");
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponse<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto) {
        UserEntity user = getActiveUser(userLoginRequestDto.getEmail());

        if (user.getUserRole().equals(UserRole.ADMIN)) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        checkIfAccountLocked(user);

        try {
            authenticate(userLoginRequestDto);
            resetFailedAttempts(user);
        } catch (Exception exception) {
            handleFailedLogin(user);
        }

        return SuccessResponse.of(generateAuthResponse(user), "Login successful.");
    }

    @Override
    public SuccessResponse<UserAuthResponseDto> adminLogin(UserLoginRequestDto userLoginRequestDto) {
        UserEntity user = userRepository.findByEmailAndUserRoleAndStatus(userLoginRequestDto.getEmail(), UserRole.ADMIN, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found!"));

        checkIfAccountLocked(user);

        try {
            authenticate(userLoginRequestDto);
            resetFailedAttempts(user);
        } catch (Exception exception) {
            handleFailedLogin(user);
        }

        return SuccessResponse.of(generateAuthResponse(user), "Login successful.");
    }

    @Override
    @Transactional
    public SuccessResponse<UserAuthResponseDto> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String refreshToken = refreshTokenRequestDto.getRefreshToken();
        String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception exception) {
            throw new InvalidCredentialsException("Invalid or expired refresh token.");
        }

        if (Objects.isNull(userEmail)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token.");
        }

        var userEntity = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User associated with token not found."));

        var userDetails = withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidCredentialsException("Refresh token validation failed.");
        }

        var newAccessToken = jwtService.generateToken(userDetails);

        var userAuthResponseDto = UserAuthResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();

        return SuccessResponse.of(userAuthResponseDto, "Refresh token refreshed successfully.");
    }

    private UserEntity getActiveUser(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));
    }

    private void checkIfAccountLocked(UserEntity user) {
        if (Objects.nonNull(user.getAccountLockedUntil()) && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Your account has been locked due to too many failed login attempts. Please try again later.");
        }
    }

    private void authenticate(UserLoginRequestDto dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
    }

    private void resetFailedAttempts(UserEntity user) {
        if (user.getFailedLoginAttempts() > 0 || Objects.nonNull(user.getAccountLockedUntil())) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }
    }

    private void handleFailedLogin(UserEntity user) {
        if (Objects.nonNull(user.getAccountLockedUntil()) && user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
        }
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);

        if (Objects.nonNull(user.getAccountLockedUntil())) {
            throw new InvalidCredentialsException("Too many failed login attempts. Your account has been locked for " + LOCK_DURATION_MINUTES + " minutes.");
        }

        throw new InvalidCredentialsException("Invalid email or password.");
    }

    private UserAuthResponseDto generateAuthResponse(UserEntity userEntity) {
        var UserDetails = withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        var accessToken = jwtService.generateToken(UserDetails);
        var refreshToken = jwtService.generateRefreshToken(UserDetails);

        return UserAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
