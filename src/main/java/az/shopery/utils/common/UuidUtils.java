package az.shopery.utils.common;

import az.shopery.handler.exception.ApplicationException;
import lombok.experimental.UtilityClass;
import java.util.UUID;

@UtilityClass
public class UuidUtils {

    public static UUID parse(String uuidString) {
        try {
            return UUID.fromString(uuidString.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException("It is not a valid UUID format!");
        }
    }
}
