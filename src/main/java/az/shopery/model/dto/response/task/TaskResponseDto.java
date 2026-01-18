package az.shopery.model.dto.response.task;

import az.shopery.model.dto.shared.TaskCreatorDto;
import az.shopery.utils.enums.TaskCategory;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponseDto {
    UUID id;
    TaskCategory taskCategory;
    TaskCreatorDto taskCreatorDto;
    Instant createdAt;
    Instant updatedAt;
}
