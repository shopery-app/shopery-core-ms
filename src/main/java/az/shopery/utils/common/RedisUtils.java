package az.shopery.utils.common;

public final class RedisUtils {

    private RedisUtils() {
    }

    public static final String REGISTER_PREFIX = "auth:register:email:";
    public static final String REGISTER_COOLDOWN = "auth:register:cooldown:";

    public static final String RESET_TOKEN_PREFIX = "auth:reset:token:";
    public static final String RESET_COOLDOWN_PREFIX = "auth:reset:cooldown:";

    public static final String EMAIL_UPDATE_PREFIX = "auth:email-update:";

    public static String registerKey(String email) {
        return REGISTER_PREFIX + email;
    }

    public static String registerCooldownKey(String email) {
        return REGISTER_COOLDOWN + email;
    }

    public static String resetTokenKey(String token) {
        return RESET_TOKEN_PREFIX + token;
    }

    public static String emailUpdateKey(String email) {
        return EMAIL_UPDATE_PREFIX + email;
    }

    public static String resetCooldownKey(String email) {
        return RESET_COOLDOWN_PREFIX + email;
    }
}
