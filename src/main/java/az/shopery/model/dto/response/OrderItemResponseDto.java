package az.shopery.model.dto.response;

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
public class OrderItemResponseDto {
    UUID productId;
    String productName;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal subtotal;
}
