package az.shopery.core_ms.model.dto.response;

import az.shopery.core_ms.model.dto.shared.DiscountDto;
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
    byte[] image;
    BigDecimal currentPrice;
    Integer stockQuantity;
    DiscountDto discountDto;
}
