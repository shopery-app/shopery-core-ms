package az.shopery.handler.notification;

import az.shopery.model.event.PasswordChangedNotificationEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordChangedNotificationHandler
        implements NotificationHandler<PasswordChangedNotificationEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.PASSWORD_CHANGED;
    }

    @Override
    public void handle(PasswordChangedNotificationEvent event) {
        emailService.sendPasswordChangedNotification(
                event.userEmail(),
                event.userName()
        );

        log.debug("Password changed notification sent to {}", event.userEmail());
    }
}

