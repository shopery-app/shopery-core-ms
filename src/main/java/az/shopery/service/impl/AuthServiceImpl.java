package az.shopery.service.impl;

import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.PhoneAlreadyExistsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ResendCodeRequestDto;
import az.shopery.model.dto.request.UserVerificationRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.VerificationTokenEntity;
import az.shopery.repository.UserRepository;
import az.shopery.repository.VerificationTokenRepository;
import az.shopery.service.AuthService;
import az.shopery.service.EmailService;
import az.shopery.utils.security.JwtService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public SuccessResponseDto<Void> register(UserRegisterRequestDto userRegisterRequestDto) {
        if (userRepository.findByPhone(userRegisterRequestDto.getPhone()).isPresent()) {
            throw new PhoneAlreadyExistsException(
                    "Phone '" + userRegisterRequestDto.getPhone() + "' is already in use.");
        }

        if (userRepository.findByEmail(userRegisterRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "Email '" + userRegisterRequestDto.getEmail() + "' is already in use.");
        }

        verificationTokenRepository.findByUserEmail(userRegisterRequestDto
                .getEmail()).ifPresent(verificationTokenRepository::delete);

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        var verificationToken = VerificationTokenEntity.builder()
                .userName(userRegisterRequestDto.getName())
                .userEmail(userRegisterRequestDto.getEmail())
                .userPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()))
                .userPhone(userRegisterRequestDto.getPhone())
                .token(passwordEncoder.encode(code))
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        try {
            verificationTokenRepository.saveAndFlush(verificationToken);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();
            if (message.contains("user_phone")) {
                throw new PhoneAlreadyExistsException(
                        "This phone number is currently being registered. Please try again later.");
            } else if (message.contains("user_email")) {
                throw new EmailAlreadyExistsException(
                        "This email address is currently being registered. Please try again later.");
            } else {
                throw new InvalidCredentialsException("Could not complete registration due to a data conflict.");
            }
        }

        emailService.sendVerificationCode(userRegisterRequestDto.getEmail(), userRegisterRequestDto.getName(), code);

        return SuccessResponseDto.of("Verification code sent to your email. Please verify to complete registration.");
    }


    @Override
    @Transactional
    public SuccessResponseDto<UserAuthResponseDto> verifyAccount(UserVerificationRequestDto verificationRequestDto) {
        VerificationTokenEntity verificationTokenEntity = verificationTokenRepository
                .findByUserEmail(verificationRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Verification token not found or already used. Please register again."));

        if (verificationTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationTokenEntity);
            throw new InvalidCredentialsException("Verification token expired. Please register again.");
        }

        if (!passwordEncoder.matches(verificationRequestDto.getCode(), verificationTokenEntity.getToken())) {
            throw new InvalidCredentialsException("Invalid verification code.");
        }

        var user = UserEntity.builder()
                .name(verificationTokenEntity.getUserName())
                .email(verificationTokenEntity.getUserEmail())
                .password(verificationTokenEntity.getUserPassword())
                .phone(verificationTokenEntity.getUserPhone())
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
                        "No pending verification code found for this email. Please register first."));

        String newCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        verificationTokenEntity.setToken(passwordEncoder.encode(newCode));
        verificationTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));

        verificationTokenRepository.save(verificationTokenEntity);

        emailService.sendVerificationCode(
                verificationTokenEntity.getUserEmail(), verificationTokenEntity.getUserName(), newCode);

        return SuccessResponseDto.of("Verification code sent to your email.");
    }

    @Override
    public SuccessResponseDto<UserAuthResponseDto> login(UserLoginRequestDto userLoginRequestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginRequestDto.getEmail(),
                            userLoginRequestDto.getPassword()
                    )
            );
        } catch (Exception exception) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        var user = userRepository.findByEmail(userLoginRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + userLoginRequestDto.getEmail()));

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
