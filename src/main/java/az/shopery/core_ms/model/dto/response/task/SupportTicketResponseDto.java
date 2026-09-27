package az.shopery.core_ms.model.dto.response.task;

import az.shopery.core_ms.utils.enums.TicketStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportTicketResponseDto extends TaskResponseDto {
    String supportTicketSubject;
    String supportTicketDescription;
    TicketStatus ticketStatus;
}
