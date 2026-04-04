package az.shopery.model.dto.response;

import java.time.Instant;
import java.time.LocalDate;
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
public class UserProfileResponseDto {
    String firstName;
    String lastName;
    String email;
    String phone;
    LocalDate dateOfBirth;
    Instant createdAt;
    String profilePhotoUrl;
}
