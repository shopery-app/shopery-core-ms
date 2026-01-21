package az.shopery.service.impl;

import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserEmailUpdateRequestDto;
import az.shopery.model.dto.request.UserEmailVerificationRequestDto;
import az.shopery.model.dto.request.UserPasswordUpdateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.BlogResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserEmailUpdateResponseDto;
import az.shopery.model.dto.response.UserPasswordUpdateResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.shared.AuthorDto;
import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.EmailUpdateTokenEntity;
import az.shopery.model.entity.SavedBlogEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.repository.BlogLikeRepository;
import az.shopery.repository.BlogRepository;
import az.shopery.repository.EmailUpdateTokenRepository;
import az.shopery.repository.SavedBlogRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.EmailService;
import az.shopery.service.UserService;
import az.shopery.utils.common.AdminAssignmentHelper;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import az.shopery.utils.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.UuidUtils.parse;

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
    private final EmailService emailService;
    private final AdminAssignmentHelper adminAssignmentHelper;
    private final BlogRepository blogRepository;
    private final SavedBlogRepository savedBlogRepository;
    private final BlogLikeRepository blogLikeRepository;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<UserProfileResponseDto> getMyProfile(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        var dto = mapToDto(userEntity);
        return SuccessResponseDto.of(dto, "User profile retrieved successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<UserProfileResponseDto> updateMyProfile(String userEmail, UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setName(userProfileUpdateRequestDto.getFirstName().trim() + " " + userProfileUpdateRequestDto.getLastName().trim());
        userEntity.setPhone(userProfileUpdateRequestDto.getPhone());
        userEntity.setDateOfBirth(userProfileUpdateRequestDto.getDateOfBirth());

        UserEntity updatedUserEntity = userRepository.save(userEntity);
        var dto = mapToDto(updatedUserEntity);
        log.info("User profile updated successfully for user {}", userEmail);
        return SuccessResponseDto.of(dto, "User profile updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);

        if (userEntity.getUserRole().equals(UserRole.MERCHANT)) {
            return SuccessResponseDto.of(null, "You are already a merchant.");
        }

        userEntity.setUserRole(UserRole.MERCHANT);
        userEntity.setLastRoleChangeAt(Instant.now());
        userRepository.save(userEntity);

        var userDetails = User.withUsername(userEmail)
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
        return SuccessResponseDto.of(dto, "Become merchant successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        UserEntity assignedAdmin = adminAssignmentHelper.assignRandomAdmin();

        if (shopRepository.existsByUser(userEntity)) {
            throw new IllegalStateException("User already has a shop.");
        }
        if (shopRepository.existsByShopName(shopCreateRequestDto.getShopName())) {
            throw new IllegalStateException("Shop with name '" + shopCreateRequestDto.getShopName() + "' already exists.");
        }

        ShopCreationRequestEntity shopCreationRequestEntity = ShopCreationRequestEntity.builder()
                .createdBy(userEntity)
                .assignedAdmin(assignedAdmin)
                .shopName(shopCreateRequestDto.getShopName())
                .description(shopCreateRequestDto.getDescription())
                .build();
        taskRepository.save(shopCreationRequestEntity);

        return SuccessResponseDto.of("Your request has been submitted and assigned to an admin for review.");
    }

    @Override
    public SuccessResponseDto<UserPasswordUpdateResponseDto> updateMyPassword(String userEmail, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if(!passwordEncoder.matches(userPasswordUpdateRequestDto.getOldPassword(), userEntity.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }
        if(userPasswordUpdateRequestDto.getNewPassword().equals(userPasswordUpdateRequestDto.getOldPassword())) {
            throw new IllegalArgumentException("New password must be different from the old password.");
        }
        userEntity.setPassword(passwordEncoder.encode(userPasswordUpdateRequestDto.getNewPassword()));
        userEntity.setPasswordChangedAt(Instant.now());
        userRepository.save(userEntity);

        var userDetails = User.withUsername(userEntity.getEmail())
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
        emailService.sendPasswordChangedNotification(userEntity.getEmail(), userEntity.getName());

        return SuccessResponseDto.of(userPasswordUpdateResponseDto, "Password has been updated successfully.");
    }

    @Override
    public SuccessResponseDto<Void> changeMyEmail(String userEmail, UserEmailUpdateRequestDto userEmailUpdateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        if(userRepository.existsByEmail(userEmailUpdateRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        EmailUpdateTokenEntity emailUpdateTokenEntity = emailUpdateTokenRepository.findByEmail(userEmailUpdateRequestDto.getEmail())
                .orElse(new EmailUpdateTokenEntity());
        emailUpdateTokenEntity.setEmail(userEmailUpdateRequestDto.getEmail());
        emailUpdateTokenEntity.setToken(code);
        emailUpdateTokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        emailUpdateTokenRepository.save(emailUpdateTokenEntity);

        emailService.sendVerificationCode(userEmailUpdateRequestDto.getEmail(), userEntity.getName(), code, Boolean.FALSE);

        return SuccessResponseDto.of("Verification code has been sent to your email address");
    }

    @Override
    public SuccessResponseDto<UserEmailUpdateResponseDto> verifyMyEmail(String userEmail, UserEmailVerificationRequestDto userEmailVerificationRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);
        EmailUpdateTokenEntity emailUpdateTokenEntity = emailUpdateTokenRepository.findByEmail(userEmailVerificationRequestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No pending verification found. It may have expired or been verified already"));

        if(emailUpdateTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailUpdateTokenRepository.delete(emailUpdateTokenEntity);
            throw new InvalidCredentialsException("Verification token expired. Please request a new one.");
        }
        if(!emailUpdateTokenEntity.getToken().equals(userEmailVerificationRequestDto.getCode())) {
            throw new InvalidCredentialsException("Invalid verification code");
        }

        emailUpdateTokenRepository.delete(emailUpdateTokenEntity);
        userEntity.setEmail(userEmailVerificationRequestDto.getEmail());
        userRepository.save(userEntity);

        var userDetails = User.withUsername(userEntity.getEmail())
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

        return SuccessResponseDto.of(userEmailUpdateResponseDto,"Email has been updated successfully");
    }

    @Override
    public SuccessResponseDto<Void> saveBlog(String userEmail, String blogId) {
        UUID parsedBlogId = parse(blogId);
        UserEntity userEntity = getUserByEmail(userEmail);
        BlogEntity blogEntity = blogRepository.findById(parsedBlogId).orElseThrow(
                () -> new ResourceNotFoundException("Blog with this id " + " not found."));

        SavedBlogEntity savedBlogEntity = SavedBlogEntity.builder()
                .blog(blogEntity)
                .user(userEntity)
                .build();

        savedBlogRepository.save(savedBlogEntity);
        return  SuccessResponseDto.of("Blog has been saved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BlogResponseDto> getSavedBlog(String userEmail, String blogId) {
        SavedBlogEntity savedBlogEntity = getUserSavedBlog(userEmail, blogId);
        BlogEntity blogEntity = savedBlogEntity.getBlog();
        return SuccessResponseDto.of(mapBlogToDto(blogEntity), "Blog has been retrieved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<BlogResponseDto>> getSavedBlogs(String userEmail, Pageable pageable) {
        UserEntity userEntity = getUserByEmail(userEmail);
        Page<SavedBlogEntity> savedBlogEntities = savedBlogRepository.findAllByUserId(userEntity.getId(), pageable);
        return SuccessResponseDto.of(savedBlogEntities.map((savedBlogEntity) -> mapBlogToDto(savedBlogEntity.getBlog())), "Blogs have been retrieved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteSavedBlog(String userEmail, String blogId) {
        SavedBlogEntity  savedBlogEntity = getUserSavedBlog(userEmail, blogId);
        savedBlogRepository.delete(savedBlogEntity);
        return  SuccessResponseDto.of("Blog has been unsaved successfully");
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private SavedBlogEntity getUserSavedBlog(String userEmail, String blogId){
        UUID parsedBlogId = parse(blogId);
        UserEntity userEntity = getUserByEmail(userEmail);
        return savedBlogRepository.findByBlogIdAndUserId(parsedBlogId, userEntity.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Saved blog with this id for the given user not found."));
    }

    private UserProfileResponseDto mapToDto(UserEntity userEntity) {
        return UserProfileResponseDto.builder()
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(userEntity.getProfilePhotoUrl())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    private BlogResponseDto mapBlogToDto(BlogEntity blogEntity) {
        return BlogResponseDto.builder()
                .id(blogEntity.getId())
                .blogTitle(blogEntity.getBlogTitle())
                .content(blogEntity.getContent())
                .imageUrl(blogEntity.getImageUrl())
                .createdAt(blogEntity.getCreatedAt())
                .updatedAt(blogEntity.getUpdatedAt())
                .likeCount(blogLikeRepository.countByBlog(blogEntity))
                .author(AuthorDto.builder()
                        .name(blogEntity.getUser().getName())
                        .profilePhotoUrl(blogEntity.getUser().getProfilePhotoUrl())
                        .build())
                .build();
    }
}
