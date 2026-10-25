package az.shopery.core_ms.utils.common;

import az.shopery.core_ms.utils.enums.AddressType;
import az.shopery.core_ms.utils.enums.ProductCategory;
import az.shopery.core_ms.utils.enums.ProductCondition;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
import az.shopery.core_ms.utils.enums.TaskCategory;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstraints {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_DURATION_MINUTES = 10;
    public static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;
    public static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    public static final int COOLDOWN_SECONDS = 60;

    public static final int EMAIL_UPDATE_CODE_EXPIRY_MINUTES = 5;

    public static final int SIX_DIGIT_VERIFICATION_CODE_MIN = 100000;
    public static final int SIX_DIGIT_VERIFICATION_CODE_MAX = 999999;

    public static final long PREMIUM_MAX_TOKENS = 100L;

    public static final Map<String, Class<? extends Enum<?>>> DROPDOWN_MAP = Map.of(
            "product-categories", ProductCategory.class,
            "product-conditions", ProductCondition.class,
            "address-types", AddressType.class,
            "task-types", TaskCategory.class,
            "subscription-tiers", SubscriptionTier.class
    );
}
