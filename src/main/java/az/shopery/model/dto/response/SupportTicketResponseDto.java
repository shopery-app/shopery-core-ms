package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.TaskCreatorDto;
import az.shopery.utils.enums.TicketStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportTicketResponseDto {
    UUID id;
    String subject;
    String description;
    TicketStatus status;
    Instant createdAt;
    Instant updatedAt;
    TaskCreatorDto creator;
}
