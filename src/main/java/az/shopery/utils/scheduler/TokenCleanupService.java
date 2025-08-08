package az.shopery.utils.scheduler;

import az.shopery.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Running scheduled job to clean up expired verification tokens...");
        LocalDateTime now = LocalDateTime.now();
        verificationTokenRepository.deleteByExpiryDateBefore(now);
        log.info("Expired token cleanup job finished at {}", now);
    }
}
