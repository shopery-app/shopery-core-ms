package az.shopery.model.dto.response;

import az.shopery.utils.enums.OrderStatus;
import java.math.BigDecimal;
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
public class OrderResponseDto {
    UUID id;
    UUID shopId;
    String shopName;
    OrderStatus status;
    BigDecimal totalPrice;
    String addressLine1;
    String addressLine2;
    String city;
    String country;
    String postalCode;
    Instant createdAt;
    List<OrderItemResponseDto> items;
}
