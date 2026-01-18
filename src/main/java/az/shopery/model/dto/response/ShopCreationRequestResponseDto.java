package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.TaskCreatorDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopCreationRequestResponseDto {
    UUID id;
    TaskCreatorDto creator;
    Instant createdAt;
}
