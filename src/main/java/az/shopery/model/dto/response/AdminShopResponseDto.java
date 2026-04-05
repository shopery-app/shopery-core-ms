package az.shopery.model.dto.response;

import az.shopery.utils.enums.ShopStatus;
import az.shopery.utils.enums.SubscriptionTier;
import az.shopery.utils.enums.UserStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class AdminShopResponseDto {
    UUID id;
    String shopName;
    String description;
    BigDecimal totalIncome;
    Double rating;
    Instant createdAt;
    Long totalProducts;
    SubscriptionTier subscriptionTier;
    ShopStatus shopStatus;
    String userEmail;
    UserStatus userStatus;
}
