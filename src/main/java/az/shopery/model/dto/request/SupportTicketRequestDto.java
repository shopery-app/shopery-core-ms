package az.shopery.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportTicketRequestDto {
    @NotBlank(message = "Subject cannot be blank!")
    String subject;
    @NotBlank(message = "Description cannot be blank!")
    String description;
}
