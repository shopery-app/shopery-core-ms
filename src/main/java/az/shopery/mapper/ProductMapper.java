package az.shopery.mapper;

import static az.shopery.utils.common.DiscountCalculator.calculateDiscountFromOriginalPrice;

import az.shopery.model.dto.response.ProductDetailResponseDto;
import az.shopery.model.dto.response.ProductResponseDto;
import az.shopery.model.dto.shared.PriceHistoryDto;
import az.shopery.model.entity.PriceHistoryEntity;
import az.shopery.model.entity.ProductEntity;
import az.shopery.utils.common.FilenetClientHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final FilenetClientHelper filenetClientHelper;

    public ProductResponseDto toBriefDto(ProductEntity productEntity) {
        var productResponseDto = ProductResponseDto.builder()
                .id(productEntity.getId())
                .productName(productEntity.getProductName())
                .description(productEntity.getDescription())
                .currentPrice(productEntity.getCurrentPrice())
                .stockQuantity(productEntity.getStockQuantity())
                .discountDto(calculateDiscountFromOriginalPrice(productEntity.getCurrentPrice(), productEntity.getOriginalPrice()))
                .build();

        if (Objects.nonNull(productEntity.getImageId())) {
            productResponseDto.setImage(filenetClientHelper.getFile(productEntity.getImageId()));
        }

        return productResponseDto;
    }

    public ProductDetailResponseDto toDetailDto(ProductEntity product) {
        List<PriceHistoryDto> historyDtos = Objects.nonNull(product.getPriceHistory())
                ? product.getPriceHistory().stream()
                .sorted(Comparator.comparing(PriceHistoryEntity::getCreatedAt).reversed())
                .map(ph -> PriceHistoryDto.builder()
                        .price(ph.getPrice())
                        .setAt(ph.getCreatedAt())
                        .build())
                .toList()
                : Collections.emptyList();

        byte[] image = null;
        if (Objects.nonNull(product.getImageId())) {
            image = filenetClientHelper.getFile(product.getImageId());
        }

        return ProductDetailResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .image(image)
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
