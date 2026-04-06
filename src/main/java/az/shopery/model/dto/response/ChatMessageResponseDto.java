package az.shopery.model.dto.response;

import az.shopery.utils.enums.MessageStatus;
import java.time.Instant;
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
public class ChatMessageResponseDto {
    UUID id;
    UUID senderId;
    UUID receiverId;
    String content;
    MessageStatus status;
    Instant createdAt;
    Instant readAt;
}
