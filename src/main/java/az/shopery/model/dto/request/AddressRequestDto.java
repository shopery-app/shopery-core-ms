package az.shopery.model.dto.request;

import az.shopery.utils.annotation.ValidEnum;
import az.shopery.utils.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AddressRequestDto {
    @NotBlank(message = "Address line 1 cannot be empty!")
    @Size(max = 100, message = "Address line 1 is too long.")
    String addressLine1;
    @Size(max = 100)
    String addressLine2;
    @NotBlank(message = "City cannot be empty!")
    @Size(max = 100, message = "City is too long.")
    String city;
    @NotBlank(message = "Country cannot be empty!")
    @Size(max = 100, message = "Country is too long.")
    String country;
    @NotBlank(message = "Postal code cannot be empty!")
    @Size(max = 20, message = "Postal code is too long.")
    String postalCode;
    @NotNull(message = "Address type is required!")
    @ValidEnum(enumClass = AddressType.class)
    private AddressType addressType;
}
