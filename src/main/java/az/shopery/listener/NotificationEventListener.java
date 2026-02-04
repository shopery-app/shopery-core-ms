package az.shopery.listener;

import az.shopery.model.event.NotificationEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailService emailService;

    @EventListener
    public void onNotification(NotificationEvent notificationEvent) {
        emailService.sendNotification(notificationEvent);
    }
}
