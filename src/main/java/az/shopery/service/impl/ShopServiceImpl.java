package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.ProductMapper;
import az.shopery.model.dto.response.ShopResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserShopResponseDto;
import az.shopery.model.entity.ShopEntity;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ShopService;
import java.util.Collections;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<UserShopResponseDto> getMyShop(String userEmail) {
        userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        ShopEntity shopEntity = shopRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for user: " + userEmail));

        var userShopResponseDto = UserShopResponseDto.builder()
                .shopName(shopEntity.getShopName())
                .description(shopEntity.getDescription())
                .totalIncome(shopEntity.getTotalIncome())
                .rating(shopEntity.getRating())
                .createdAt(shopEntity.getCreatedAt())
                .build();

        return SuccessResponse.of(userShopResponseDto, "User shop retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
        Page<ShopEntity> shopPage = shopRepository.findAllWithActiveOwners(pageable);
        Page<ShopResponseDto> dtoPage = shopPage.map(this::mapToPublicShopDtoWithoutProducts);

        log.info("Retrieved {} shops for page {} of size {}", dtoPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
        return SuccessResponse.of(dtoPage, "Shops retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<ShopResponseDto> getShopById(String shopId) {
        ShopEntity shopEntity = shopRepository.findActiveShopByIdWithProducts(parse(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for id: " + shopId));
        var shopResponseDto = mapToPublicShopDtoWithProducts(shopEntity);

        log.info("Shop retrieved successfully for id {}", shopId);
        return SuccessResponse.of(shopResponseDto, "Shop retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<ShopResponseDto> getShopByShopName(String shopName) {
        ShopEntity shopEntity = shopRepository.findActiveShopByShopNameWithProducts(shopName)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for name: " + shopName));
        var shopResponseDto = mapToPublicShopDtoWithProducts(shopEntity);

        log.info("Shop retrieved successfully for name {}", shopName);
        return SuccessResponse.of(shopResponseDto, "Shop retrieved successfully.");
    }

    private ShopResponseDto mapToPublicShopDtoWithoutProducts(ShopEntity shopEntity) {
        return ShopResponseDto.builder()
                .id(shopEntity.getId())
                .shopName(shopEntity.getShopName())
                .description(shopEntity.getDescription())
                .rating(shopEntity.getRating())
                .createdAt(shopEntity.getCreatedAt())
                .products(Collections.emptyList())
                .build();
    }

    private ShopResponseDto mapToPublicShopDtoWithProducts(ShopEntity shopEntity) {
        return ShopResponseDto.builder()
                .id(shopEntity.getId())
                .shopName(shopEntity.getShopName())
                .description(shopEntity.getDescription())
                .rating(shopEntity.getRating())
                .createdAt(shopEntity.getCreatedAt())
                .products(shopEntity.getProducts().stream()
                        .map(productMapper::toBriefDto)
                        .toList())
                .build();
    }
}
