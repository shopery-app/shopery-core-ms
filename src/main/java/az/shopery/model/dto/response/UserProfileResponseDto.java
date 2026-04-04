package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.ShopSummaryDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
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
    UUID id;
    String firstName;
    String lastName;
    String email;
    String phone;
    LocalDate dateOfBirth;
    Instant createdAt;
    String profilePhotoUrl;
    ShopSummaryDto shop;
}
