package az.shopery.core_ms.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEmailUpdateResponseDto {
    String accessToken;
    String refreshToken;
    UserProfileResponseDto userProfileResponseDto;
}
