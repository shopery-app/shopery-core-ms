package az.shopery.utils.common;

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
}
