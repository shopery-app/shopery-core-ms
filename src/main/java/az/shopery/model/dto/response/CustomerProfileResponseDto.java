package az.shopery.model.dto.response;

import java.time.Instant;
import java.util.Date;
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
public class CustomerProfileResponseDto {
    String firstName;
    String lastName;
    String email;
    String phone;
    Date dateOfBirth;
    Instant createdAt;
    String profilePhotoUrl;
}
