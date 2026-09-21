package az.shopery.model.dto.response.task;

import az.shopery.model.dto.shared.TaskCreatorDto;
import az.shopery.utils.enums.TaskCategory;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "taskCategory", visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShopCreationRequestResponseDto.class, name = "SHOP_CREATION_REQUEST"),
        @JsonSubTypes.Type(value = SupportTicketResponseDto.class, name = "SUPPORT_TICKET")
})
public class TaskResponseDto {
    UUID id;
    TaskCategory taskCategory;
    TaskCreatorDto taskCreatorDto;
    Instant createdAt;
    Instant updatedAt;
}
