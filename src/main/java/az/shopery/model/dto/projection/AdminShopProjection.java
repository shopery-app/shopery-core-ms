package az.shopery.model.dto.projection;

import az.shopery.utils.enums.ShopStatus;
import az.shopery.utils.enums.SubscriptionTier;
import az.shopery.utils.enums.UserStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface AdminShopProjection {
    UUID getId();
    String getShopName();
    String getDescription();
    BigDecimal getTotalIncome();
    Double getRating();
    Instant getCreatedAt();
    Long getTotalProducts();
    SubscriptionTier getSubscriptionTier();
    ShopStatus getShopStatus();
    String getUserEmail();
    UserStatus getUserStatus();
}
