package az.shopery.handler.notification;

import az.shopery.model.event.OrderConfirmationNotificationEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApprovedNotificationHandler
        implements NotificationHandler<OrderConfirmationNotificationEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.ORDER_CONFIRMED;
    }

    @Override
    public void handle(OrderConfirmationNotificationEvent event) {
        emailService.sendOrderConfirmation(
                event.userEmail(),
                event.userName(),
                event.orderIds()
        );

        log.debug("Order approved notification sent to {}", event.userEmail());
    }
}

