package az.shopery.handler.notification;

import az.shopery.model.event.PasswordResetLinkEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetLinkHandler
        implements NotificationHandler<PasswordResetLinkEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.PASSWORD_RESET_LINK;
    }

    @Override
    public void handle(PasswordResetLinkEvent event) {
        emailService.sendPasswordResetLink(
                event.userEmail(),
                event.userName(),
                event.token()
        );

        log.debug("Password reset link sent to {}", event.userEmail());
    }
}

