package az.shopery.core_ms.model.dto.response;

import az.shopery.core_ms.utils.enums.AddressType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("default")
    boolean isDefault;
    AddressType addressType;
}
