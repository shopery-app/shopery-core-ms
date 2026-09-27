package az.shopery.core_ms.model.event;

import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.utils.enums.TaskCategory;
import java.util.Map;

public record TaskEvent(
        UserEntity createdBy,
        TaskCategory category,
        Map<String, Object> params) {
}
