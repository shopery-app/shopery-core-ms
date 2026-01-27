package az.shopery.utils.common;

import az.shopery.utils.enums.AddressType;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import az.shopery.utils.enums.TaskCategory;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstraints {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int LOCK_DURATION_MINUTES = 10;
    public static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;
    public static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    public static final int COOLDOWN_SECONDS = 60;

    public static final int SIX_DIGIT_VERIFICATION_CODE_MIN = 100000;
    public static final int SIX_DIGIT_VERIFICATION_CODE_MAX = 999999;

    public static final int MAX_ADDRESSES_PER_USER = 6;

    public static final Map<String, Class<? extends Enum<?>>> DROPDOWN_MAP = Map.of(
            "product-categories", ProductCategory.class,
            "product-conditions", ProductCondition.class,
            "address-types", AddressType.class,
            "task-types", TaskCategory.class
    );
}
