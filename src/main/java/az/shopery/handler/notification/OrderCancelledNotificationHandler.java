package az.shopery.handler.notification;

import az.shopery.model.event.OrderCancelledNotificationEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledNotificationHandler
        implements NotificationHandler<OrderCancelledNotificationEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.ORDER_CANCELLED;
    }

    @Override
    public void handle(OrderCancelledNotificationEvent event) {
        emailService.sendMerchantClosedNotification(
                event.email(),
                event.customerName(),
                event.merchantName()
        );

        log.debug("Order cancelled notification sent to {}", event.email());
    }
}

