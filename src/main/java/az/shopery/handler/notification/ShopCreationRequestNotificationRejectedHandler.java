package az.shopery.handler.notification;

import az.shopery.model.event.ShopCreationRequestRejectedEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShopCreationRequestNotificationRejectedHandler
        implements NotificationHandler<ShopCreationRequestRejectedEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.SHOP_REJECTED;
    }

    @Override
    public void handle(ShopCreationRequestRejectedEvent event) {
        emailService.sendShopRejectedEmail(
                event.creatorEmail(),
                event.creatorName(),
                event.shopName(),
                event.rejectionReason()
        );

        log.debug("Shop creation rejected notification sent to {}", event.creatorEmail());
    }
}

