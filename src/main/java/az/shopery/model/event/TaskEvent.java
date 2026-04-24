package az.shopery.model.event;

import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.TaskCategory;
import java.util.Map;

public record TaskEvent(
        UserEntity createdBy,
        TaskCategory category,
        Map<String, Object> params) {
}
