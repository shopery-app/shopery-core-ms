package az.shopery.mapper;

import static az.shopery.utils.common.DiscountCalculator.calculateDiscountFromOriginalPrice;

import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.shared.PriceHistoryDto;
import az.shopery.model.entity.PriceHistoryEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.utils.aws.S3FileUtil;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final S3FileUtil s3FileUtil;

    public ProductResponseDto toBriefDto(ProductEntity productEntity) {
        return ProductResponseDto.builder()
                .id(productEntity.getId())
                .productName(productEntity.getProductName())
                .description(productEntity.getDescription())
                .imageUrl(s3FileUtil.generatePresignedUrl(productEntity.getImageUrl()))
                .currentPrice(productEntity.getCurrentPrice())
                .discountDto(calculateDiscountFromOriginalPrice(productEntity.getCurrentPrice(), productEntity.getOriginalPrice()))
                .build();
    }

    public ProductDetailResponseDto toDetailDto(ProductEntity product) {
        List<PriceHistoryDto> historyDtos =
                Objects.nonNull(product.getPriceHistory())
                        ? product.getPriceHistory().stream()
                        .sorted(Comparator.comparing(PriceHistoryEntity::getCreatedAt).reversed())
                        .map(ph -> PriceHistoryDto.builder()
                                .price(ph.getPrice())
                                .setAt(ph.getCreatedAt())
                                .build())
                        .toList()
                        : Collections.emptyList();

        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageUrl(s3FileUtil.generatePresignedUrl(product.getImageUrl()))
                .currentPrice(product.getCurrentPrice())
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
}
