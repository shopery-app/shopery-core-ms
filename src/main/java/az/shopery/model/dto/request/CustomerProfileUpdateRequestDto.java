package az.shopery.model.dto.request;

import az.shopery.utils.annotation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerProfileUpdateRequestDto {
    @NotBlank(message = "First name cannot be empty!")
    @Size(max = 30, message = "First name is too long.")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "First name cannot contain special characters!")
    String firstName;
    @NotBlank(message = "Last name cannot be empty!")
    @Size(max = 30, message = "Last name is too long.")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Last name cannot contain special characters!")
    String lastName;
    @Size(max = 30, message = "Phone is too long.")
    @ValidPhone
    String phone;
    @Past(message = "Date of birth must be a past date")
    Date dateOfBirth;
}
