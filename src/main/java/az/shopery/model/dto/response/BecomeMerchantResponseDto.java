package az.shopery.model.dto.response;

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
public class BecomeMerchantResponseDto {
    String accessToken;
    String refreshToken;
    MerchantProfileResponseDto merchantProfileResponseDto;
}
