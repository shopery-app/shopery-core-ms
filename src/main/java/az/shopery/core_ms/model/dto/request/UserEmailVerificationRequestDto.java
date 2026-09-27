package az.shopery.core_ms.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEmailVerificationRequestDto {
    @Email(message = "Email is not valid!")
    @NotBlank(message = "Email cannot be empty!")
    String email;
    @NotBlank(message = "Code cannot be empty!")
    @Size(min = 6, max = 6, message = "Code must be exactly 6 digits long.")
    @Pattern(regexp = "^[0-9]+$", message = "Code must contain only numbers.")
    private String code;
}
