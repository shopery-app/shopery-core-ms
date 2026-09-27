package az.shopery.core_ms.model.dto.redis;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CachedVerificationData {
    @ToString.Exclude
    String hashedToken;
    @ToString.Exclude
    String userEmail;
    @ToString.Exclude
    String hashedPassword;
    String userName;
    int attemptCount;
    LocalDateTime expiryDate;
    LocalDateTime codeLastSentAt;
}
