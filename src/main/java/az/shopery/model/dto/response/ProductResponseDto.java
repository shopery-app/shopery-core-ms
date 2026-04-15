package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.DiscountDto;
import java.math.BigDecimal;
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
public class ProductResponseDto {
    UUID id;
    String productName;
    String description;
    String imageUrl;
    BigDecimal currentPrice;
    Integer stockQuantity;
    DiscountDto discountDto;
}
