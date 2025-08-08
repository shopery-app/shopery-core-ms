package az.shopery.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserVerificationRequestDto {
    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "Email is not valid!")
    String email;
    @NotBlank(message = "Code cannot be empty!")
    @Size(min = 6, max = 6, message = "Code must be 6 characters long!")
    String code;
}
