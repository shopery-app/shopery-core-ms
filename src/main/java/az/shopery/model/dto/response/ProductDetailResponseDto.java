package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.DiscountDto;
import az.shopery.model.dto.shared.PriceHistoryDto;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponseDto {
    UUID id;
    String productName;
    String description;
    String imageUrl;
    DiscountDto discountDto;
    Integer stockQuantity;
    ProductCategory category;
    ProductCondition condition;
    String shopName;
    UUID shopId;
    List<PriceHistoryDto> priceHistory;
    Instant createdAt;
}
