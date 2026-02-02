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
import az.shopery.model.dto.request.ForgotPasswordRequestDto;
import az.shopery.model.dto.request.RefreshTokenRequestDto;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.ResetPasswordRequestDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.entity.PasswordResetTokenEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.VerificationTokenEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.model.event.PasswordChangedNotificationEvent;
import az.shopery.model.event.PasswordResetLinkEvent;
import az.shopery.model.event.VerificationCodeEvent;
import az.shopery.repository.PasswordResetTokenRepository;
import az.shopery.repository.UserRepository;
import az.shopery.repository.VerificationTokenRepository;
import az.shopery.service.AuthService;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.UserStatus;
import az.shopery.utils.enums.VerificationProgress;
import az.shopery.utils.security.JwtService;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public SuccessResponseDto<Void> register(UserRegisterRequestDto userRegisterRequestDto) {
        if (userRepository.findByEmailAndStatus(userRegisterRequestDto.getEmail(), UserStatus.ACTIVE).isPresent()) {
            throw new EmailAlreadyExistsException("Email '" + userRegisterRequestDto.getEmail() + "' is already in use.");
        }

        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository.findByUserEmail(userRegisterRequestDto.getEmail()).orElse(new VerificationTokenEntity());

        String code = generateSixDigitVerificationCode();

        verificationTokenEntity.setToken(passwordEncoder.encode(code));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        verificationTokenEntity.setUserName(userRegisterRequestDto.getName());
        verificationTokenEntity.setUserEmail(userRegisterRequestDto.getEmail());
        verificationTokenEntity.setUserPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
        verificationTokenEntity.setCodeLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationTokenEntity);

        applicationEventPublisher.publishEvent(new NotificationEvent<>(
                NotificationType.VERIFICATION_CODE,
                new VerificationCodeEvent(
                        userRegisterRequestDto.getEmail(),
                        userRegisterRequestDto.getName(),
                        code,
                        Boolean.TRUE
                )
        ));
        return SuccessResponseDto.of("Verification code sent to your email. Please verify to complete registration.");
    }


    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponseDto<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto) {
        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository.findByUserEmailAndProgress(verificationRequestDto.getEmail(), VerificationProgress.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found. It may have expired or been verified already."));

        if (verificationTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenEntity.setProgress(VerificationProgress.REJECTED);
            throw new InvalidCredentialsException("Verification token expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(verificationRequestDto.getCode(), verificationTokenEntity.getToken())) {
            verificationTokenEntity.setAttemptCount(verificationTokenEntity.getAttemptCount() + 1);
            if (verificationTokenEntity.getAttemptCount() >= MAX_FAILED_ATTEMPTS) {
                verificationTokenEntity.setProgress(VerificationProgress.REJECTED);
                throw new InvalidCredentialsException("Invalid verification code. You have exceeded the maximum number of attempts.");
            }
            throw new InvalidCredentialsException("Invalid verification code.");
        }

        var user = UserEntity.builder()
                .name(verificationTokenEntity.getUserName())
                .email(verificationTokenEntity.getUserEmail())
                .password(verificationTokenEntity.getUserPassword())
                .build();
        userRepository.save(user);

        verificationTokenEntity.setProgress(VerificationProgress.VERIFIED);
        verificationTokenRepository.save(verificationTokenEntity);

        return SuccessResponseDto.of(generateAuthResponse(user), "Account verified successfully. Welcome to Shopery!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto) {
        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository.findByUserEmail(resendCodeRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No registration process found for this email. Please register first."));

        if (userRepository.findByEmailAndStatus(resendCodeRequestDto.getEmail(), UserStatus.ACTIVE).isPresent()) {
            throw new EmailAlreadyExistsException("This account has already been verified.");
        }

        LocalDateTime lastSent = verificationTokenEntity.getCodeLastSentAt();
        if (Objects.nonNull(lastSent)) {
            LocalDateTime cooldownEndTime = lastSent.plusSeconds(COOLDOWN_SECONDS);
            if (LocalDateTime.now().isBefore(cooldownEndTime)) {
                long secondsRemaining = Duration.between(LocalDateTime.now(), cooldownEndTime).getSeconds();
                throw new CooldownNotMetException("Please wait " + secondsRemaining + " seconds before resending the code.");
            }
        }

        String newCode = generateSixDigitVerificationCode();

        verificationTokenEntity.setToken(passwordEncoder.encode(newCode));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setCodeLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationTokenEntity);

        applicationEventPublisher.publishEvent(new NotificationEvent<>(
                NotificationType.VERIFICATION_CODE,
                new VerificationCodeEvent(
                        verificationTokenEntity.getUserEmail(),
                        verificationTokenEntity.getUserName(),
                        newCode,
                        Boolean.TRUE
                )
        ));
        return SuccessResponseDto.of("A new verification code has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        var userEntity = userRepository.findByEmailAndStatus(forgotPasswordRequestDto.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + forgotPasswordRequestDto.getEmail()));

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByUserEmail(userEntity.getEmail()).orElse(new PasswordResetTokenEntity());

        LocalDateTime lastSent = passwordResetTokenEntity.getLinkLastSentAt();
        if (Objects.nonNull(lastSent)) {
            LocalDateTime cooldownEndTime = lastSent.plusSeconds(COOLDOWN_SECONDS);
            if (LocalDateTime.now().isBefore(cooldownEndTime)) {
                long secondsRemaining = Duration.between(LocalDateTime.now(), cooldownEndTime).getSeconds();
                throw new CooldownNotMetException("Please wait " + secondsRemaining + " seconds before requesting a new password reset link.");
            }
        }

        passwordResetTokenEntity.setToken(UUID.randomUUID().toString());
        passwordResetTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        passwordResetTokenEntity.setUserEmail(userEntity.getEmail());
        passwordResetTokenEntity.setLinkLastSentAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetTokenEntity);


        applicationEventPublisher.publishEvent(new NotificationEvent<>(
                NotificationType.PASSWORD_RESET_LINK,
                new PasswordResetLinkEvent(
                        userEntity.getEmail(),
                        userEntity.getName(),
                        passwordResetTokenEntity.getToken()
                )
        ));
        return SuccessResponseDto.of("A new password reset link has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        var resetToken = passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired password reset token."));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidCredentialsException("Password reset token expired. Please request a new one.");
        }

        var user = userRepository.findByEmailAndStatus(resetToken.getUserEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for the given token."));

        user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        applicationEventPublisher.publishEvent(new NotificationEvent<>(
                NotificationType.PASSWORD_CHANGED,
                new PasswordChangedNotificationEvent(
                        user.getEmail(),
                        user.getName()
                )
        ));
        return SuccessResponseDto.of("Password reset successful.");
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponseDto<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto) {
        UserEntity user = userRepository.findByEmailAndStatus(userLoginRequestDto.getEmail(), UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (Objects.nonNull(user.getAccountLockedUntil()) && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Your account has been locked due to too many failed login attempts. Please try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginRequestDto.getEmail(),
                            userLoginRequestDto.getPassword()
                    )
            );
            if (user.getFailedLoginAttempts() > 0 || Objects.nonNull(user.getAccountLockedUntil())) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
            }
        } catch (Exception exception) {
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
            } else {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
        }

        return SuccessResponseDto.of(generateAuthResponse(user), "Login successful.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<UserAuthResponseDto> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
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

        return SuccessResponseDto.of(userAuthResponseDto, "Refresh token refreshed successfully.");
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
