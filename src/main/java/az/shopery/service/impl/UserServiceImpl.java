package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ShopCreateRequestDto;
import az.shopery.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserService;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.security.JwtService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ShopRepository shopRepository;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<UserProfileResponseDto> getMyProfile(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        var dto = UserProfileResponseDto.builder()
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(userEntity.getProfilePhotoUrl())
                .createdAt(userEntity.getCreatedAt())
                .build();
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
        var dto = UserProfileResponseDto.builder()
                .firstName(first(updatedUserEntity.getName()))
                .lastName(last(updatedUserEntity.getName()))
                .email(updatedUserEntity.getEmail())
                .phone(updatedUserEntity.getPhone())
                .dateOfBirth(updatedUserEntity.getDateOfBirth())
                .profilePhotoUrl(updatedUserEntity.getProfilePhotoUrl())
                .createdAt(updatedUserEntity.getCreatedAt())
                .build();
        log.info("User profile updated successfully for user {}", userEmail);
        return SuccessResponseDto.of(dto, "User profile updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);

        if (userEntity.getUserRole() == UserRole.MERCHANT) {
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
                .userProfileResponseDto(UserProfileResponseDto.builder()
                        .firstName(first(userEntity.getName()))
                        .lastName(last(userEntity.getName()))
                        .email(userEntity.getEmail())
                        .phone(userEntity.getPhone())
                        .dateOfBirth(userEntity.getDateOfBirth())
                        .profilePhotoUrl(userEntity.getProfilePhotoUrl())
                        .createdAt(userEntity.getCreatedAt())
                        .build())
                .build();
        log.info("Become merchant successfully for user {}", userEmail);
        return SuccessResponseDto.of(dto, "Become merchant successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> createMyShop(String userEmail, ShopCreateRequestDto shopCreateRequestDto) {
        UserEntity userEntity = getUserByEmail(userEmail);

        if (shopRepository.existsByUser(userEntity)) {
            throw new IllegalStateException("User already has a shop.");
        }

        if (shopRepository.existsByShopName(shopCreateRequestDto.getShopName())) {
            throw new IllegalStateException("Shop with name '" + shopCreateRequestDto.getShopName() + "' already exists.");
        }

        ShopEntity newShop = ShopEntity.builder()
                .user(userEntity)
                .shopName(shopCreateRequestDto.getShopName())
                .description(shopCreateRequestDto.getDescription())
                .totalIncome(BigDecimal.ZERO)
                .rating(0.0)
                .build();
        ShopEntity savedShop = shopRepository.save(newShop);
        log.info("New shop '{}' created successfully for user {}", savedShop.getShopName(), userEmail);

        return SuccessResponseDto.of(null, "Shop created successfully.");
    }

    private UserEntity getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private String first(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        return parts.isEmpty() ? "" : parts.getFirst();
    }

    private String last(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        if (parts.size() <= 1) return "";
        return String.join(" ", parts.subList(1, parts.size()));
    }
}
