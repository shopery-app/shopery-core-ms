package az.shopery.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UserRegisterRequestDto {
    @NotBlank(message = "Name cannot be empty!")
    String name;
    @Email(message = "Email is not valid!")
    @NotBlank(message = "Email cannot be empty!")
    String email;
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters long.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    @NotBlank(message = "Password cannot be empty!")
    String password;
    @NotBlank(message = "Phone cannot be empty!")
    String phone;
}
