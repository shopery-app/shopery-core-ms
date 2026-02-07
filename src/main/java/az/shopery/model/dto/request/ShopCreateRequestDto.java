package az.shopery.model.dto.request;

import az.shopery.utils.annotation.ValidEnum;
import az.shopery.utils.enums.SubscriptionTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "Shop name cannot contain special characters!")
    String shopName;
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description;
    @NotNull(message = "Subscription tier is required!")
    @ValidEnum(enumClass = SubscriptionTier.class, excluded = {"NONE"})
    SubscriptionTier subscriptionTier;
}
