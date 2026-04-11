package az.shopery.model.dto.redis;

import az.shopery.utils.enums.VerificationProgress;
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
    VerificationProgress progress;
    LocalDateTime expiryDate;
    LocalDateTime codeLastSentAt;
}
