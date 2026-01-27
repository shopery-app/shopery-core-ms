package az.shopery.utils.common;

import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class NameMapperHelper {

    public static String first(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        return parts.isEmpty() ? StringUtils.EMPTY : parts.getFirst();
    }

    public static String last(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        if (parts.size() <= 1){
            return StringUtils.EMPTY;
        }
        return String.join(StringUtils.SPACE, parts.subList(1, parts.size()));
    }
}
