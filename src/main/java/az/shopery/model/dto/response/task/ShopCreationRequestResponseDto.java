package az.shopery.model.dto.response.task;

import az.shopery.utils.enums.RequestStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopCreationRequestResponseDto extends TaskResponseDto {
    String shopName;
    String shopDescription;
    String rejectionReason;
    RequestStatus requestStatus;
}
