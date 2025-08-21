package az.shopery.service.impl;

import az.shopery.handler.exception.InvalidUuidFormatException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ProductCreateRequestDto;
import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.shared.DiscountDto;
import az.shopery.model.dto.shared.PriceHistoryDto;
import az.shopery.model.entity.PriceHistoryEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ProductRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ProductService;
import az.shopery.utils.aws.FileStorageService;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public SuccessResponseDto<ProductDetailResponseDto> addProduct(String userEmail, ProductCreateRequestDto productCreateRequestDto) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);

        ProductEntity productEntity = ProductEntity.builder()
                .shop(shopEntity)
                .productName(productCreateRequestDto.getProductName())
                .description(productCreateRequestDto.getDescription())
                .currentPrice(productCreateRequestDto.getPrice())
                .originalPrice(productCreateRequestDto.getPrice())
                .stockQuantity(productCreateRequestDto.getStockQuantity())
                .category(ProductCategory.valueOf(productCreateRequestDto.getCategory()))
                .condition(ProductCondition.valueOf(productCreateRequestDto.getCondition()))
                .priceHistory(new ArrayList<>())
                .build();
        PriceHistoryEntity initialPrice = PriceHistoryEntity.builder()
                .product(productEntity)
                .price(productCreateRequestDto.getPrice())
                .build();
        productEntity.getPriceHistory().add(initialPrice);

        ProductEntity savedProductEntity = productRepository.save(productEntity);
        log.info("New product '{}' created successfully for shop {}", savedProductEntity.getProductName(), shopEntity.getShopName());
        return SuccessResponseDto.of(mapToDetailDto(savedProductEntity), "Product created successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<ProductDetailResponseDto> updateProduct(String userEmail, String productId, ProductCreateRequestDto productCreateRequestDto) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);
        UUID id = parse(productId);
        ProductEntity productEntity = getProductForShop(id, shopEntity.getId());

        if (productEntity.getCurrentPrice().compareTo(productCreateRequestDto.getPrice()) != 0) {
            PriceHistoryEntity oldPrice = PriceHistoryEntity.builder()
                    .product(productEntity)
                    .price(productEntity.getCurrentPrice())
                    .build();
            productEntity.getPriceHistory().add(oldPrice);
            productEntity.setCurrentPrice(productCreateRequestDto.getPrice());
        }

        productEntity.setProductName(productCreateRequestDto.getProductName());
        productEntity.setDescription(productCreateRequestDto.getDescription());
        productEntity.setStockQuantity(productCreateRequestDto.getStockQuantity());
        productEntity.setCategory(ProductCategory.valueOf(productCreateRequestDto.getCategory()));
        productEntity.setCondition(ProductCondition.valueOf(productCreateRequestDto.getCondition()));

        ProductEntity updatedProductEntity = productRepository.save(productEntity);
        return SuccessResponseDto.of(mapToDetailDto(updatedProductEntity), "Product updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<String> updateProductImage(String userEmail, String productId, MultipartFile imageFile) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);
        UUID id = parse(productId);
        ProductEntity productEntity = getProductForShop(id, shopEntity.getId());

        String oldImageUrlKey = productEntity.getImageUrl();
        String newImageUrlKey = fileStorageService.store(imageFile);

        productEntity.setImageUrl(newImageUrlKey);
        productRepository.save(productEntity);

        if (oldImageUrlKey != null) {
            fileStorageService.delete(oldImageUrlKey);
        }

        String presignedUrl = generatePresignedUrl(newImageUrlKey);
        log.info("Product image updated successfully for product {}", productEntity.getProductName());
        return SuccessResponseDto.of(presignedUrl, "Product image updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteProductImage(String userEmail, String productId) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);
        UUID id = parse(productId);
        ProductEntity productEntity = getProductForShop(id, shopEntity.getId());

        String imageKey = productEntity.getImageUrl();
        if (imageKey == null || imageKey.isBlank()) {
            throw new ResourceNotFoundException("No product image found for product: " + productId);
        }

        fileStorageService.delete(imageKey);
        productEntity.setImageUrl(null);
        productRepository.save(productEntity);
        log.info("Product image deleted successfully for product {}", productEntity.getProductName());
        return SuccessResponseDto.of(null, "Product image deleted successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> deleteProduct(String userEmail, String productId) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);
        UUID id = parse(productId);
        ProductEntity productEntity = getProductForShop(id, shopEntity.getId());

        String imageKey = productEntity.getImageUrl();
        productRepository.delete(productEntity);
        if (imageKey != null) {
            fileStorageService.delete(imageKey);
        }
        log.info("Product '{}' deleted successfully for shop {}", productEntity.getProductName(), shopEntity.getShopName());
        return SuccessResponseDto.of(null, "Product deleted successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<Page<ProductResponseDto>> getMyProducts(String userEmail, Pageable pageable) {
        ShopEntity shopEntity = getShopForMerchant(userEmail);
        Page<ProductEntity> productEntityPage = productRepository.findByShopId(shopEntity.getId(), pageable);
        return SuccessResponseDto.of(productEntityPage.map(this::mapToBriefDto), "Your products retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<Page<ProductResponseDto>> searchPublicProducts(ProductCategory category, Pageable pageable) {
        Page<ProductEntity> productEntityPage = (category != null)
                ? productRepository.findByCategory(category, pageable)
                : productRepository.findAll(pageable);
        return SuccessResponseDto.of(productEntityPage.map(this::mapToBriefDto), "Products retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<ProductDetailResponseDto> getPublicProductById(String productId) {
        UUID id = parse(productId);
        ProductEntity productEntity = productRepository.findByIdWithPriceHistory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return SuccessResponseDto.of(mapToDetailDto(productEntity), "Product retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<Page<ProductResponseDto>> getTopDiscountedProducts(Pageable pageable) {
        Page<ProductEntity> productEntityPage = productRepository.findTopDiscountedProducts(pageable);
        Page<ProductResponseDto> productResponseDtoPage = productEntityPage.map(this::mapToBriefDto);

        log.info("Top discounted products retrieved successfully for page {} of size {}", productResponseDtoPage.getTotalElements(), pageable.getPageSize());
        return SuccessResponseDto.of(productResponseDtoPage, "Top discounted products retrieved successfully.");
    }

    private ShopEntity getShopForMerchant(String userEmail) {
        UserEntity userEntity = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return shopRepository.findByUser(userEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for this merchant. Please create a shop first."));
    }

    private ProductEntity getProductForShop(UUID productId, UUID shopId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        UUID actualShopId;
        try {
            actualShopId = product.getShop().getId();
        } catch (Exception e) {
            throw new IllegalStateException("Could not determine shop ownership for product: " + productId);
        }

        if (!actualShopId.equals(shopId)) {
            throw new ResourceNotFoundException("You do not have product with id: " + productId + " in your shop.");
        }
        return product;
    }

    private ProductResponseDto mapToBriefDto(ProductEntity product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageUrl(generatePresignedUrl(product.getImageUrl()))
                .discountDto(calculateDiscountFromOriginalPrice(product.getCurrentPrice(), product.getOriginalPrice()))
                .build();
    }

    private ProductDetailResponseDto mapToDetailDto(ProductEntity product) {
        List<PriceHistoryDto> historyDtos = (product.getPriceHistory() != null) ?
                product.getPriceHistory().stream()
                        .sorted(Comparator.comparing(PriceHistoryEntity::getCreatedAt).reversed())
                        .map(ph -> PriceHistoryDto.builder()
                                .price(ph.getPrice())
                                .setAt(ph.getCreatedAt())
                                .build())
                        .toList() :
                Collections.emptyList();

        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageUrl(generatePresignedUrl(product.getImageUrl()))
                .discountDto(calculateDiscountFromOriginalPrice(product.getCurrentPrice(), product.getOriginalPrice()))
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .condition(product.getCondition())
                .shopName(product.getShop().getShopName())
                .shopId(product.getShop().getId())
                .priceHistory(historyDtos)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private UUID parse(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException exception) {
            throw new InvalidUuidFormatException("It is not a valid UUID format!");
        }
    }

    private String generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileKey).build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key: {}", fileKey, e);
            return null;
        }
    }

    private DiscountDto calculateDiscountFromOriginalPrice(BigDecimal currentPrice, BigDecimal originalPrice) {
        if (originalPrice == null || currentPrice.compareTo(originalPrice) >= 0) {
            return null;
        }

        BigDecimal difference = originalPrice.subtract(currentPrice);
        BigDecimal percentageDecimal = difference.divide(originalPrice, 2, RoundingMode.HALF_UP);
        int percentage = percentageDecimal.multiply(new BigDecimal("100")).intValue();
        return DiscountDto.builder()
                .percentage(percentage)
                .originalPrice(originalPrice)
                .currentPrice(currentPrice)
                .build();
    }
}
