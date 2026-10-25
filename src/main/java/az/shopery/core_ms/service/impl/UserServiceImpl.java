package az.shopery.core_ms.service.impl;

import static az.shopery.core_ms.utils.common.CommonConstraints.EMAIL_UPDATE_CODE_EXPIRY_MINUTES;
import static az.shopery.core_ms.utils.common.VerificationCodeGenerator.generateSixDigitVerificationCode;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.shopery.core_ms.client.UserClient;
import az.shopery.core_ms.handler.exception.ApplicationException;
import az.shopery.core_ms.handler.exception.EmailAlreadyExistsException;
import az.shopery.core_ms.handler.exception.InvalidCredentialsException;
import az.shopery.core_ms.handler.exception.ResourceNotFoundException;
import az.shopery.core_ms.kafka.producer.NotificationProducer;
import az.shopery.core_ms.model.dto.redis.CachedEmailUpdateData;
import az.shopery.core_ms.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.core_ms.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.core_ms.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.core_ms.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.core_ms.model.dto.response.UserProfileResponseDto;
import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.model.event.NotificationEvent;
import az.shopery.core_ms.repository.UserRepository;
import az.shopery.core_ms.service.RedisService;
import az.shopery.core_ms.service.UserService;
import az.shopery.core_ms.utils.common.RedisUtils;
import az.shopery.core_ms.utils.enums.NotificationType;
import az.shopery.core_ms.utils.enums.UserStatus;
import az.shopery.core_ms.utils.security.JwtService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserClient userClient;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationProducer notificationProducer;

    @Override
    public SuccessResponse<UserPasswordUpdateResponseDto> updateMyPassword(String userEmail, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if (!passwordEncoder.matches(userPasswordUpdateRequestDto.getOldPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials!");
        }
        if (userPasswordUpdateRequestDto.getNewPassword().equals(userPasswordUpdateRequestDto.getOldPassword())) {
            throw new ApplicationException("New password must be different from the old password!");
        }
        userEntity.setPassword(passwordEncoder.encode(userPasswordUpdateRequestDto.getNewPassword()));
        userEntity.setPasswordChangedAt(Instant.now());
        userRepository.save(userEntity);

        var userDetails = withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        var userPasswordUpdateResponseDto = UserPasswordUpdateResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userProfileResponseDto(getProfile(userEntity.getEmail()))
                .build();

        notificationProducer.send(
                new NotificationEvent(
                        userEmail,
                        NotificationType.PASSWORD_CHANGED,
                        Map.of(
                                "userName",userEntity.getName()
                        )
                )
        );
        return SuccessResponse.of(userPasswordUpdateResponseDto, "Password has been updated successfully!");
    }

    @Override
    public SuccessResponse<Void> changeMyEmail(String userEmail, UserEmailUpdateRequestDto userEmailUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if (userRepository.existsByEmail(userEmailUpdateRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        String code = generateSixDigitVerificationCode();

        CachedEmailUpdateData cachedEmailUpdateData = CachedEmailUpdateData.builder()
                .code(code)
                .requestedByEmail(userEmail)
                .build();

        redisService.set(
                RedisUtils.emailUpdateKey(userEmailUpdateRequestDto.getEmail()),
                cachedEmailUpdateData,
                Duration.ofMinutes(EMAIL_UPDATE_CODE_EXPIRY_MINUTES)
        );

        notificationProducer.send(
                new NotificationEvent(
                        userEmail,
                        NotificationType.VERIFICATION_CODE,
                        Map.of(
                                "userName", userEntity.getName(),
                                "isRegistration", Boolean.FALSE,
                                "verificationCode", code
                        )
                )
        );
        return SuccessResponse.of("Verification code has been sent to your email address!");
    }

    @Override
    public SuccessResponse<UserEmailUpdateResponseDto> verifyMyEmail(String userEmail, UserEmailVerificationRequestDto userEmailVerificationRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);

        CachedEmailUpdateData cachedEmailUpdateData = redisService
                .get(RedisUtils.emailUpdateKey(userEmailVerificationRequestDto.getEmail()), CachedEmailUpdateData.class)
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found! It may have expired or been verified already!"));

        if (!cachedEmailUpdateData.getRequestedByEmail().equals(userEmail)) {
            throw new InvalidCredentialsException("Invalid verification request!");
        }

        if (!cachedEmailUpdateData.getCode().equals(userEmailVerificationRequestDto.getCode())) {
            throw new InvalidCredentialsException("Invalid verification code!");
        }

        userEntity.setEmail(userEmailVerificationRequestDto.getEmail());
        userRepository.save(userEntity);

        redisService.delete(RedisUtils.emailUpdateKey(userEmailVerificationRequestDto.getEmail()));

        var userDetails = withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        var userEmailUpdateResponseDto = UserEmailUpdateResponseDto.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .refreshToken(jwtService.generateRefreshToken(userDetails))
                .userProfileResponseDto(getProfile(userEntity.getEmail()))
                .build();

        return SuccessResponse.of(userEmailUpdateResponseDto,"Email has been updated successfully!");
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private UserProfileResponseDto getProfile(String userEmail) {
        var response = userClient.getMyProfile(userEmail);

        if (Objects.isNull(response.getBody())) {
            return null;
        }

        return response.getBody().getData();
    }
}
