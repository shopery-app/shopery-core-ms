package az.shopery.model.dto.request;

import az.shopery.utils.annotation.ValidEnum;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequestDto {
    @NotBlank(message = "Product name cannot be empty!")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters long.")
    @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "Product name cannot contain special characters!")
    String productName;
    @Size(max = 2000, message = "Maximum product description length exceeded!")
    @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "Product description cannot contain special characters!")
    String description;
    @NotNull(message = "Product condition is required!")
    @ValidEnum(enumClass = ProductCondition.class)
    ProductCondition condition;
    @NotNull(message = "Product category is required!")
    @ValidEnum(enumClass = ProductCategory.class)
    ProductCategory category;
    @NotNull
    @Positive
    BigDecimal price;
    @NotNull
    @PositiveOrZero
    Integer stockQuantity;
}
