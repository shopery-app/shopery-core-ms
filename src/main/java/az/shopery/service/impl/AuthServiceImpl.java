package az.shopery.service.impl;

import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ForgotPasswordRequestDto;
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
import java.time.LocalDateTime;
import java.util.Optional;
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
                verificationTokenRepository.findByUserEmail(userRegisterRequestDto
                .getEmail())
                .orElse(new VerificationTokenEntity());

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        verificationTokenEntity.setToken(passwordEncoder.encode(code));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        verificationTokenEntity.setUserName(userRegisterRequestDto.getName());
        verificationTokenEntity.setUserEmail(userRegisterRequestDto.getEmail());
        verificationTokenEntity.setUserPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
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

        String newCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        verificationTokenEntity.setToken(passwordEncoder.encode(newCode));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        verificationTokenEntity.setProgress(VerificationProgress.PENDING);
        verificationTokenEntity.setAttemptCount(0);
        verificationTokenEntity.setUserName(verificationTokenEntity.getUserName());
        verificationTokenEntity.setUserEmail(verificationTokenEntity.getUserEmail());
        verificationTokenEntity.setUserPassword(verificationTokenEntity.getUserPassword());
        verificationTokenRepository.save(verificationTokenEntity);

        emailService.sendVerificationCode(
                verificationTokenEntity.getUserEmail(), verificationTokenEntity.getUserName(), newCode);

        return SuccessResponseDto.of("A new verification code has been sent to your email.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        var user = userRepository.findByEmail(forgotPasswordRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + forgotPasswordRequestDto.getEmail()));

        Optional<PasswordResetTokenEntity> existingTokenOpt = passwordResetTokenRepository
                .findByUserEmail(user.getEmail());

        PasswordResetTokenEntity passwordResetToken;

        if (existingTokenOpt.isPresent()) {
            passwordResetToken = existingTokenOpt.get();
            passwordResetToken.setToken(UUID.randomUUID().toString());
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        } else {
            passwordResetToken = PasswordResetTokenEntity.builder()
                    .token(UUID.randomUUID().toString())
                    .userEmail(user.getEmail())
                    .expiryDate(LocalDateTime.now().plusMinutes(15))
                    .build();
        }

        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetLink(user.getEmail(), user.getName(), passwordResetToken.getToken());

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

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
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
            if (user.getFailedLoginAttempts() > 0 || user.getAccountLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
            }
        } catch (Exception exception) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                throw new InvalidCredentialsException("Too many failed login attempts. " +
                        "Your account has been locked for " + LOCK_DURATION_MINUTES + " minutes.");
            } else {
                throw new InvalidCredentialsException("Invalid email or password.");
            }
        }

        var userDetails = org.springframework.security.core.userdetails.User
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
}
