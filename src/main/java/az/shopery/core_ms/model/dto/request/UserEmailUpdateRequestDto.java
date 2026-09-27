package az.shopery.core_ms.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEmailUpdateRequestDto {
    @Email(message = "Email is not valid!")
    @NotBlank(message = "Email cannot be empty!")
    String email;
}
