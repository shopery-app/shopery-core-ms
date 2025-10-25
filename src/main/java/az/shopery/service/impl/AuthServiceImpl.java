package az.shopery.service.impl;

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
import az.shopery.repository.PasswordResetTokenRepository;
import az.shopery.repository.UserRepository;
import az.shopery.repository.VerificationTokenRepository;
import az.shopery.service.AuthService;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.VerificationProgress;
import az.shopery.utils.security.JwtService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 10;
    private static final int COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public SuccessResponseDto<Void> register(UserRegisterRequestDto userRegisterRequestDto) {
        if (userRepository.findByEmail(userRegisterRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "Email '" + userRegisterRequestDto.getEmail() + "' is already in use.");
        }

        VerificationTokenEntity verificationTokenEntity =
                verificationTokenRepository.findByUserEmail(userRegisterRequestDto.getEmail())
                        .orElse(new VerificationTokenEntity());

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        verificationTokenEntity.setToken(passwordEncoder.encode(code));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        verificationTokenEntity.setUserName(userRegisterRequestDto.getName());
        verificationTokenEntity.setUserEmail(userRegisterRequestDto.getEmail());
        verificationTokenEntity.setUserPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
        verificationTokenEntity.setCodeLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationTokenEntity);

        emailService.sendVerificationCode(userRegisterRequestDto.getEmail(), userRegisterRequestDto.getName(), code);

        return SuccessResponseDto.of("Verification code sent to your email. Please verify to complete registration.");
    }


    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponseDto<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto) {
        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository
                .findByUserEmailAndProgress(verificationRequestDto.getEmail(), VerificationProgress.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending verification found. It may have expired or been verified already."));

        if (verificationTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenEntity.setProgress(VerificationProgress.REJECTED);
            throw new InvalidCredentialsException("Verification token expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(verificationRequestDto.getCode(), verificationTokenEntity.getToken())) {
            verificationTokenEntity.setAttemptCount(verificationTokenEntity.getAttemptCount() + 1);
            if (verificationTokenEntity.getAttemptCount() >= MAX_FAILED_ATTEMPTS) {
                verificationTokenEntity.setProgress(VerificationProgress.REJECTED);
                throw new InvalidCredentialsException(
                        "Invalid verification code. You have exceeded the maximum number of attempts.");
            }
            throw new InvalidCredentialsException("Invalid verification code.");
        }

        var user = UserEntity.builder()
                .name(verificationTokenEntity.getUserName())
                .email(verificationTokenEntity.getUserEmail())
                .password(verificationTokenEntity.getUserPassword())
                .build();
        userRepository.save(user);

        var UserDetails = User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getUserRole().name())
                .build();

        var accessToken = jwtService.generateToken(UserDetails);
        var refreshToken = jwtService.generateRefreshToken(UserDetails);

        verificationTokenRepository.delete(verificationTokenEntity);

        var authResponse = UserAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return SuccessResponseDto.of(authResponse, "Account verified successfully. Welcome to Shopery!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> resendVerificationCode(ResendCodeRequestDto resendCodeRequestDto) {
        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository
                .findByUserEmail(resendCodeRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No registration process found for this email. Please register first."));

        if (userRepository.findByEmail(resendCodeRequestDto.getEmail()).isPresent()) {
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

        String newCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        verificationTokenEntity.setToken(passwordEncoder.encode(newCode));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setCodeLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationTokenEntity);

        emailService.sendVerificationCode(
                verificationTokenEntity.getUserEmail(), verificationTokenEntity.getUserName(), newCode);

        return SuccessResponseDto.of("A new verification code has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        var userEntity = userRepository.findByEmail(forgotPasswordRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + forgotPasswordRequestDto.getEmail()));

        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository
                .findByUserEmail(userEntity.getEmail()).orElse(new PasswordResetTokenEntity());

        LocalDateTime lastSent = passwordResetTokenEntity.getLinkLastSentAt();
        if (Objects.nonNull(lastSent)) {
            LocalDateTime cooldownEndTime = lastSent.plusSeconds(COOLDOWN_SECONDS);
            if (LocalDateTime.now().isBefore(cooldownEndTime)) {
                long secondsRemaining = Duration.between(LocalDateTime.now(), cooldownEndTime).getSeconds();
                throw new CooldownNotMetException("Please wait " + secondsRemaining + " seconds before requesting a new password reset link.");
            }
        }

        passwordResetTokenEntity.setToken(UUID.randomUUID().toString());
        passwordResetTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        passwordResetTokenEntity.setUserEmail(userEntity.getEmail());
        passwordResetTokenEntity.setLinkLastSentAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetTokenEntity);

        emailService.sendPasswordResetLink(userEntity.getEmail(), userEntity.getName(), passwordResetTokenEntity.getToken());
        return SuccessResponseDto.of("A new password reset link has been sent to your email.");

    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        var resetToken = passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid or expired password reset token."));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidCredentialsException("Password reset token expired. Please request a new one.");
        }

        var user = userRepository.findByEmail(resetToken.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for the given token."));

        user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return SuccessResponseDto.of("Password reset successful.");
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public SuccessResponseDto<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto) {
        UserEntity user = userRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (Objects.nonNull(user.getAccountLockedUntil()) && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Your account has been locked due to too many " +
                    "failed login attempts. Please try again later.");
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
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            }
            userRepository.save(user);

            if (Objects.nonNull(user.getAccountLockedUntil())) {
                throw new InvalidCredentialsException("Too many failed login attempts. " +
                        "Your account has been locked for " + LOCK_DURATION_MINUTES + " minutes.");
            } else {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
        }

        var userDetails = User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getUserRole().name())
                .build();

        var accessToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        var authResponse = UserAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return SuccessResponseDto.of(authResponse, "Login successful.");
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

        var userEntity = this.userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User associated with token not found."));

        var userDetails = User.withUsername(userEntity.getEmail())
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
}
