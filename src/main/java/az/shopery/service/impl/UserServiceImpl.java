package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.EMAIL_UPDATE_CODE_EXPIRY_MINUTES;
import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.VerificationCodeGenerator.generateSixDigitVerificationCode;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.redis.CachedEmailUpdateData;
import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.shared.ShopSummaryDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.RedisService;
import az.shopery.service.UserService;
import az.shopery.utils.aws.S3FileUtil;
import az.shopery.utils.common.AdminAssignmentHelper;
import az.shopery.utils.common.RedisUtils;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.ShopStatus;
import az.shopery.utils.enums.UserStatus;
import az.shopery.utils.security.JwtService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final TaskRepository taskRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AdminAssignmentHelper adminAssignmentHelper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final S3FileUtil s3FileUtil;
    private final RedisService redisService;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<UserProfileResponseDto> getMyProfile(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        return SuccessResponse.of(mapToDto(userEntity), "User profile retrieved successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<UserProfileResponseDto> updateMyProfile(String userEmail, UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setName(userProfileUpdateRequestDto.getFirstName().trim() + " " + userProfileUpdateRequestDto.getLastName().trim());
        userEntity.setPhone(userProfileUpdateRequestDto.getPhone());
        userEntity.setDateOfBirth(userProfileUpdateRequestDto.getDateOfBirth());

        UserEntity updatedUserEntity = userRepository.save(userEntity);
        log.info("User profile updated successfully for user {}", userEmail);
        return SuccessResponse.of(mapToDto(updatedUserEntity), "User profile updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        UserEntity assignedAdmin = adminAssignmentHelper.assignRandomAdmin();

        if (shopRepository.existsByUserAndStatus(userEntity, ShopStatus.ACTIVE)) {
            throw new IllegalRequestException("User already has an active shop.");
        }

        if (shopRepository.existsByUserAndStatus(userEntity, ShopStatus.PENDING)) {
            throw new IllegalRequestException("User already has a pending shop.");
        }

        if (shopRepository.existsByShopName(shopCreateRequestDto.getShopName())) {
            throw new IllegalRequestException("Shop with name '" + shopCreateRequestDto.getShopName() + "' already exists.");
        }

        ShopCreationRequestEntity shopCreationRequestEntity = ShopCreationRequestEntity.builder()
                .createdBy(userEntity)
                .assignedAdmin(assignedAdmin)
                .shopName(shopCreateRequestDto.getShopName())
                .description(shopCreateRequestDto.getDescription())
                .subscriptionTier(shopCreateRequestDto.getSubscriptionTier())
                .build();
        taskRepository.save(shopCreationRequestEntity);

        ShopEntity shop = ShopEntity.builder()
                .user(shopCreationRequestEntity.getCreatedBy())
                .shopName(shopCreationRequestEntity.getShopName())
                .description(shopCreationRequestEntity.getDescription())
                .totalIncome(BigDecimal.ZERO)
                .rating(0.0)
                .status(ShopStatus.PENDING)
                .build();
        shopRepository.save(shop);

        return SuccessResponse.of("Your request has been submitted and assigned to an admin for review.");
    }

    @Override
    public SuccessResponse<UserPasswordUpdateResponseDto> updateMyPassword(String userEmail, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if (!passwordEncoder.matches(userPasswordUpdateRequestDto.getOldPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }
        if (userPasswordUpdateRequestDto.getNewPassword().equals(userPasswordUpdateRequestDto.getOldPassword())) {
            throw new IllegalRequestException("New password must be different from the old password.");
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
                .userProfileResponseDto(mapToDto(userEntity))
                .build();

        applicationEventPublisher.publishEvent(new NotificationEvent(
                userEmail,
                NotificationType.PASSWORD_CHANGED,
                Map.of(
                        "userName",userEntity.getName()
                )
        ));
        return SuccessResponse.of(userPasswordUpdateResponseDto, "Password has been updated successfully.");
    }

    @Override
    public SuccessResponse<Void> changeMyEmail(String userEmail, UserEmailUpdateRequestDto userEmailUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if (userRepository.existsByEmail(userEmailUpdateRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
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

        applicationEventPublisher.publishEvent(new NotificationEvent(
                userEmail,
                NotificationType.VERIFICATION_CODE,
                Map.of(
                        "userName", userEntity.getName(),
                        "isRegistration", Boolean.FALSE,
                        "verificationCode", code
                )
        ));
        return SuccessResponse.of("Verification code has been sent to your email address");
    }

    @Override
    public SuccessResponse<UserEmailUpdateResponseDto> verifyMyEmail(String userEmail, UserEmailVerificationRequestDto userEmailVerificationRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);

        CachedEmailUpdateData cachedEmailUpdateData = redisService
                .get(RedisUtils.emailUpdateKey(userEmailVerificationRequestDto.getEmail()), CachedEmailUpdateData.class)
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found! It may have expired or been verified already!"));

        if (!cachedEmailUpdateData.getRequestedByEmail().equals(userEmail)) {
            throw new InvalidCredentialsException("Invalid verification request.");
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
                .userProfileResponseDto(mapToDto(userEntity))
                .build();

        return SuccessResponse.of(userEmailUpdateResponseDto,"Email has been updated successfully!");
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private UserProfileResponseDto mapToDto(UserEntity userEntity) {
        return UserProfileResponseDto.builder()
                .id(userEntity.getId())
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(s3FileUtil.generatePresignedUrl(userEntity.getProfilePhotoUrl()))
                .createdAt(userEntity.getCreatedAt())
                .shop(mapShop(userEntity))
                .build();
    }

    private ShopSummaryDto mapShop(UserEntity userEntity) {
        return shopRepository
                .findByUserAndStatus(userEntity, ShopStatus.ACTIVE)
                .map(shop -> ShopSummaryDto.builder()
                        .id(shop.getId())
                        .shopName(shop.getShopName())
                        .status(shop.getStatus())
                        .build())
                .orElse(null);
    }
}
