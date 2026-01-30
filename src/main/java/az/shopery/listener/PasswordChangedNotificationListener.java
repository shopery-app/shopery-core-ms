package az.shopery.listener;

import az.shopery.model.event.PasswordChangedNotificationEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PasswordChangedNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChangedNotificationEvent(PasswordChangedNotificationEvent event) {
        try {
            emailService.sendPasswordChangedNotification(
                    event.userEmail(),
                    event.userName()
            );
        } catch (Exception e) {
            log.error("Failed to send password changed notification to user {}", event.userEmail(), e);
        }
    }
}
