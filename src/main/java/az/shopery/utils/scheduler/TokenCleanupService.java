package az.shopery.utils.scheduler;

import az.shopery.repository.PasswordResetTokenRepository;
import az.shopery.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running job to clean up expired verification tokens older than {}", now);
        verificationTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Expired token cleanup job finished at {}", now);
    }

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void purgeExpiredPasswordResetTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running job to clean up expired password reset tokens older than {}", now);
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Expired password reset token cleanup job finished.");
    }
}
