package az.shopery.model.dto.request;

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
public class ShopCreateRequestDto {
    @NotBlank(message = "Shop name cannot be empty.")
    @Size(min = 3, max = 40, message = "Shop name must be between 3 and 40 characters.")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Shop name cannot contain special characters!")
    String shopName;
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Shop description cannot contain special characters!")
    String description;
}
