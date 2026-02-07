package az.shopery.model.dto.response;

import az.shopery.utils.enums.SubscriptionTier;
import java.math.BigDecimal;
import java.time.Instant;
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
public class UserShopResponseDto {
    UUID id;
    String shopName;
    String description;
    BigDecimal totalIncome;
    Double rating;
    Instant createdAt;
    SubscriptionTier subscriptionTier;
}
