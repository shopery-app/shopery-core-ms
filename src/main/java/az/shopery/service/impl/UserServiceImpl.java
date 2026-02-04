package az.shopery.service.impl;

import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.VerificationCodeGenerator.generateSixDigitVerificationCode;
import static org.springframework.security.core.userdetails.User.withUsername;

import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.entity.EmailUpdateTokenEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.EmailUpdateTokenRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserService;
import az.shopery.utils.aws.S3FileUtil;
import az.shopery.utils.common.AdminAssignmentHelper;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import az.shopery.utils.security.JwtService;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private final EmailUpdateTokenRepository emailUpdateTokenRepository;
    private final TaskRepository taskRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AdminAssignmentHelper adminAssignmentHelper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final S3FileUtil s3FileUtil;

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
    public SuccessResponse<BecomeMerchantResponseDto> becomeMerchant(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);

        if (userEntity.getUserRole().equals(UserRole.MERCHANT)) {
            throw new IllegalRequestException("User is already registered as a merchant.");
        }

        userEntity.setUserRole(UserRole.MERCHANT);
        userEntity.setLastRoleChangeAt(Instant.now());
        userRepository.save(userEntity);

        var userDetails = withUsername(userEmail)
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        var dto = BecomeMerchantResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userProfileResponseDto(mapToDto(userEntity))
                .build();
        log.info("Become merchant successfully for user {}", userEmail);
        return SuccessResponse.of(dto, "Become merchant successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        UserEntity assignedAdmin = adminAssignmentHelper.assignRandomAdmin();

        if (shopRepository.existsByUser(userEntity)) {
            throw new IllegalRequestException("User already has a shop.");
        }
        if (shopRepository.existsByShopName(shopCreateRequestDto.getShopName())) {
            throw new IllegalRequestException("Shop with name '" + shopCreateRequestDto.getShopName() + "' already exists.");
        }

        ShopCreationRequestEntity shopCreationRequestEntity = ShopCreationRequestEntity.builder()
                .createdBy(userEntity)
                .assignedAdmin(assignedAdmin)
                .shopName(shopCreateRequestDto.getShopName())
                .description(shopCreateRequestDto.getDescription())
                .build();
        taskRepository.save(shopCreationRequestEntity);

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
        EmailUpdateTokenEntity emailUpdateTokenEntity = emailUpdateTokenRepository.findByEmail(userEmailUpdateRequestDto.getEmail())
                .orElse(new EmailUpdateTokenEntity());
        emailUpdateTokenEntity.setEmail(userEmailUpdateRequestDto.getEmail());
        emailUpdateTokenEntity.setToken(code);
        emailUpdateTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        emailUpdateTokenRepository.save(emailUpdateTokenEntity);

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
        EmailUpdateTokenEntity emailUpdateTokenEntity = emailUpdateTokenRepository.findByEmail(userEmailVerificationRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found. It may have expired or been verified already"));

        if (emailUpdateTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailUpdateTokenRepository.delete(emailUpdateTokenEntity);
            throw new InvalidCredentialsException("Verification token expired. Please request a new one.");
        }
        if (!emailUpdateTokenEntity.getToken().equals(userEmailVerificationRequestDto.getCode())) {
            throw new InvalidCredentialsException("Invalid verification code");
        }

        emailUpdateTokenRepository.delete(emailUpdateTokenEntity);
        userEntity.setEmail(userEmailVerificationRequestDto.getEmail());
        userRepository.save(userEntity);

        var userDetails = withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        var userEmailUpdateResponseDto = UserEmailUpdateResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userProfileResponseDto(mapToDto(userEntity))
                .build();

        return SuccessResponse.of(userEmailUpdateResponseDto,"Email has been updated successfully");
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private UserProfileResponseDto mapToDto(UserEntity userEntity) {
        return UserProfileResponseDto.builder()
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(s3FileUtil.generatePresignedUrl(userEntity.getProfilePhotoUrl()))
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
