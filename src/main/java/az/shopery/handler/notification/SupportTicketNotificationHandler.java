package az.shopery.handler.notification;

import az.shopery.model.event.SupportTicketClosedNotificationEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupportTicketNotificationHandler
        implements NotificationHandler<SupportTicketClosedNotificationEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.SUPPORT_TICKET_CLOSED;
    }

    @Override
    public void handle(SupportTicketClosedNotificationEvent event) {
        emailService.sendSupportTicketClosedNotification(
                event.userEmail(),
                event.userName(),
                event.subject(),
                event.ticketId()
        );

        log.debug("Support ticket closed notification sent to {}", event.userEmail());
    }
}

