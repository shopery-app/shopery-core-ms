package az.shopery.core_ms.model.dto.shared;

import az.shopery.core_ms.utils.enums.ShopStatus;
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
public class ShopSummaryDto {
    UUID id;
    String shopName;
    ShopStatus status;
}
