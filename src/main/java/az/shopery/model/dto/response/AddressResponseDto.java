package az.shopery.model.dto.response;

import az.shopery.utils.enums.AddressType;
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
public class AddressResponseDto {
    UUID id;
    String addressLine1;
    String addressLine2;
    String city;
    String country;
    String postalCode;
    boolean isDefault;
    AddressType addressType;
}
