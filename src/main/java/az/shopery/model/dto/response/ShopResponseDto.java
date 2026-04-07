package az.shopery.model.dto.response;

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
public class ShopResponseDto {
    UUID id;
    String shopName;
    String description;
    Double rating;
    Instant createdAt;
    UUID sellerId;
    List<ProductResponseDto> products;
}
