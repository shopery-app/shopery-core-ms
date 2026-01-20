package az.shopery.mapper;

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
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = "spring",
        injectionStrategy = org.mapstruct.InjectionStrategy.CONSTRUCTOR,
        imports = { az.shopery.utils.common.DiscountCalculator.class })
public abstract class ProductMapper {

    @Setter(onMethod_ = @Autowired)
    protected S3FileUtil s3FileUtil;

    @Mapping(
            target = "imageUrl",
            expression = "java(s3FileUtil.generatePresignedUrl(productEntity.getImageUrl()))"
    )
    @Mapping(
            target = "discountDto",
            expression = "java(DiscountCalculator.calculateDiscountFromOriginalPrice("
                    + "productEntity.getCurrentPrice(), productEntity.getOriginalPrice()))"
    )
    public abstract ProductResponseDto toBriefDto(ProductEntity productEntity);

    @Mapping(
            target = "imageUrl",
            expression = "java(s3FileUtil.generatePresignedUrl(product.getImageUrl()))"
    )
    @Mapping(
            target = "discountDto",
            expression = "java(DiscountCalculator.calculateDiscountFromOriginalPrice("
                    + "product.getCurrentPrice(), product.getOriginalPrice()))"
    )
    @Mapping(target = "shopName", source = "shop.shopName")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "priceHistory", source = "priceHistory")

    public abstract ProductDetailResponseDto toDetailDto(ProductEntity product);

    protected List<PriceHistoryDto> mapPriceHistory(List<PriceHistoryEntity> history) {
        return Objects.nonNull(history) ? history.stream()
                .sorted(Comparator.comparing(PriceHistoryEntity::getCreatedAt).reversed())
                .map(ph -> PriceHistoryDto.builder()
                        .price(ph.getPrice())
                        .setAt(ph.getCreatedAt())
                        .build())
                .toList() :  Collections.emptyList();
    }
}
