package az.shopery.utils.scheduler;

import az.shopery.repository.PasswordResetTokenRepository;
import az.shopery.repository.VerificationTokenRepository;
import az.shopery.utils.enums.VerificationProgress;
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

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running job to clean up expired or abandoned verification tokens older than {}", now);
        verificationTokenRepository.deleteByProgressOrExpiryDateBefore(VerificationProgress.REJECTED, now);
        log.info("Expired and abandoned token cleanup job finished at {}", now);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredPasswordResetTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running job to clean up expired password reset tokens older than {}", now);
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Expired and abandoned password reset token cleanup job finished.");
    }
}
