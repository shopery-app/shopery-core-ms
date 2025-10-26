package az.shopery.utils.common;

import lombok.experimental.UtilityClass;
import java.util.Arrays;

@UtilityClass
public class NameMapperHelper {

    public static String first(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        return parts.isEmpty() ? "" : parts.getFirst();
    }

    public static String last(String name) {
        var parts = Arrays.stream(name.trim().split("\\s+")).toList();
        if (parts.size() <= 1) return "";
        return String.join(" ", parts.subList(1, parts.size()));
    }
}
