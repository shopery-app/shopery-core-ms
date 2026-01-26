package az.shopery.utils.common;

import static az.shopery.utils.common.CommonConstraints.SIX_DIGIT_VERIFICATION_CODE_MAX;
import static az.shopery.utils.common.CommonConstraints.SIX_DIGIT_VERIFICATION_CODE_MIN;

import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VerificationCodeGenerator {

    public static String generateSixDigitVerificationCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(SIX_DIGIT_VERIFICATION_CODE_MIN, SIX_DIGIT_VERIFICATION_CODE_MAX + 1));
    }
}
