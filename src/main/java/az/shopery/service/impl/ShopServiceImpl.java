package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.response.ShopResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserShopResponseDto;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ShopService;
import java.util.Collections;
import az.shopery.utils.common.DiscountCalculator;
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

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<UserShopResponseDto> getMyShop(String userEmail) {
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

        return SuccessResponseDto.of(userShopResponseDto, "User shop retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<Page<ShopResponseDto>> getAllShops(Pageable pageable) {
        Page<ShopEntity> shopPage = shopRepository.findAllWithActiveOwners(pageable);
        Page<ShopResponseDto> dtoPage = shopPage.map(this::mapToPublicShopDtoWithoutProducts);

        log.info("Retrieved {} shops for page {} of size {}", dtoPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
        return SuccessResponseDto.of(dtoPage, "Shops retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<ShopResponseDto> getShopById(String shopId) {
        ShopEntity shopEntity = shopRepository.findActiveShopByIdWithProducts(parse(shopId))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for id: " + shopId));
        var shopResponseDto = mapToPublicShopDtoWithProducts(shopEntity);

        log.info("Shop retrieved successfully for id {}", shopId);
        return SuccessResponseDto.of(shopResponseDto, "Shop retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<ShopResponseDto> getShopByShopName(String shopName) {
        ShopEntity shopEntity = shopRepository.findActiveShopByShopNameWithProducts(shopName)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for name: " + shopName));
        var shopResponseDto = mapToPublicShopDtoWithProducts(shopEntity);

        log.info("Shop retrieved successfully for name {}", shopName);
        return SuccessResponseDto.of(shopResponseDto, "Shop retrieved successfully.");
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
                        .map(this::mapToPublicProductDto)
                        .toList())
                .build();
    }

    private ProductResponseDto mapToPublicProductDto(ProductEntity productEntity) {
        return ProductResponseDto.builder()
                .id(productEntity.getId())
                .productName(productEntity.getProductName())
                .description(productEntity.getDescription())
                .imageUrl(productEntity.getImageUrl())
                .currentPrice(productEntity.getCurrentPrice())
                .discountDto(DiscountCalculator.calculateDiscountFromOriginalPrice(
                        productEntity.getCurrentPrice(),
                        productEntity.getOriginalPrice()))
                .build();
    }
}
