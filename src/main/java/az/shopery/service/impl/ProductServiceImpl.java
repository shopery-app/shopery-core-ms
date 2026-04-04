package az.shopery.service.impl;

import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.ProductMapper;
import az.shopery.model.dto.request.ProductCreateRequestDto;
import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.PriceHistoryEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ProductRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ProductService;
import az.shopery.utils.aws.S3FileUtil;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import az.shopery.utils.enums.UserStatus;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final S3FileUtil s3FileUtil;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public SuccessResponse<ProductDetailResponseDto> addProduct(String userEmail, ProductCreateRequestDto productCreateRequestDto) {
        ShopEntity shopEntity = getShopForUser(userEmail);

        ProductEntity productEntity = ProductEntity.builder()
                .shop(shopEntity)
                .productName(productCreateRequestDto.getProductName())
                .description(productCreateRequestDto.getDescription())
                .currentPrice(productCreateRequestDto.getPrice())
                .originalPrice(productCreateRequestDto.getPrice())
                .stockQuantity(productCreateRequestDto.getStockQuantity())
                .category(productCreateRequestDto.getCategory())
                .condition(productCreateRequestDto.getCondition())
                .priceHistory(new ArrayList<>())
                .build();
        PriceHistoryEntity initialPrice = PriceHistoryEntity.builder()
                .product(productEntity)
                .price(productCreateRequestDto.getPrice())
                .build();
        productEntity.getPriceHistory().add(initialPrice);

        ProductEntity savedProductEntity = productRepository.save(productEntity);
        log.info("Product '{}' created for shop {}", savedProductEntity.getProductName(), shopEntity.getShopName());
        return SuccessResponse.of(productMapper.toDetailDto(savedProductEntity), "Product created successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<ProductDetailResponseDto> updateProduct(String userEmail, String productId, ProductCreateRequestDto productCreateRequestDto) {
        ProductEntity productEntity = getProductForUser(userEmail, productId);

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
        productEntity.setCategory(productCreateRequestDto.getCategory());
        productEntity.setCondition(productCreateRequestDto.getCondition());

        ProductEntity updatedProductEntity = productRepository.save(productEntity);
        return SuccessResponse.of(productMapper.toDetailDto(updatedProductEntity), "Product updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<String> updateProductImage(String userEmail, String productId, MultipartFile imageFile) {
        ProductEntity productEntity = getProductForUser(userEmail, productId);

        String newImageUrlKey = s3FileUtil.uploadNewFile(productEntity.getImageUrl(), imageFile);
        productEntity.setImageUrl(newImageUrlKey);
        productRepository.save(productEntity);

        return SuccessResponse.of(generateImageUrl(newImageUrlKey), "Product image updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteProductImage(String userEmail, String productId) {
        ProductEntity productEntity = getProductForUser(userEmail, productId);

        String imageKey = productEntity.getImageUrl();
        if (StringUtils.isBlank(imageKey)) {
            throw new ResourceNotFoundException("No product image found for product: " + productId);
        }

        s3FileUtil.deleteFileIfExists(imageKey);
        productEntity.setImageUrl(null);
        productRepository.save(productEntity);

        return SuccessResponse.of("Product image deleted successfully.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteProduct(String userEmail, String productId) {
        ProductEntity productEntity = getProductForUser(userEmail, productId);

        String imageKey = productEntity.getImageUrl();
        productRepository.delete(productEntity);
        s3FileUtil.deleteFileIfExists(imageKey);

        return SuccessResponse.of("Product deleted successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<ProductResponseDto>> getMyProducts(String userEmail, Pageable pageable) {
        ShopEntity shopEntity = getShopForUser(userEmail);
        Page<ProductEntity> productEntityPage = productRepository.findByShopId(shopEntity.getId(), pageable);
        return SuccessResponse.of(productEntityPage.map(productMapper::toBriefDto), "Your products retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<ProductResponseDto>> searchPublicProducts(ProductCategory category, ProductCondition condition, Double minPrice, Double maxPrice, String keyword, Pageable pageable) {
        Page<ProductEntity> products = productRepository.searchPublicProducts(category, condition, minPrice, maxPrice, keyword, pageable);
        return SuccessResponse.of(products.map(productMapper::toBriefDto), "Products retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<ProductDetailResponseDto> getPublicProductById(String productId) {
        ProductEntity productEntity = productRepository.findByIdWithPriceHistory(parse(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return SuccessResponse.of(productMapper.toDetailDto(productEntity), "Product retrieved successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<ProductResponseDto>> getTopDiscountedProducts(Pageable pageable) {
        Page<ProductEntity> productEntityPage = productRepository.findTopDiscountedProducts(pageable);
        return SuccessResponse.of(productEntityPage.map(productMapper::toBriefDto), "Top discounted products retrieved successfully.");
    }

    private ShopEntity getShopForUser(String userEmail) {
        UserEntity userEntity = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return shopRepository.findByUser(userEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for this user. Please create a shop first."));
    }

    private ProductEntity getProductForShop(UUID productId, UUID shopId) {
        ProductEntity product = productRepository.findByIdWithShop(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getShop().getId().equals(shopId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return product;
    }

    private ProductEntity getProductForUser(String userEmail, String productId) {
        return getProductForShop(parse(productId), getShopForUser(userEmail).getId());
    }

    private String generateImageUrl(String key) {
        return Objects.isNull(key) ? null : s3FileUtil.generatePresignedUrl(key);
    }
}
