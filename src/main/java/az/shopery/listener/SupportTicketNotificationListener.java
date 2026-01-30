package az.shopery.listener;

import az.shopery.model.event.SupportTicketClosedNotificationEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class SupportTicketNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSupportTicketClosedNotification(SupportTicketClosedNotificationEvent event) {
        try {
            emailService.sendSupportTicketClosedNotification(
                    event.userEmail(),
                    event.userName(),
                    event.subject(),
                    event.ticketId()
            );
        } catch (Exception e) {
            log.error("Failed to send support ticket closed email for ticket {}", event.ticketId(), e);
        }
    }
}
