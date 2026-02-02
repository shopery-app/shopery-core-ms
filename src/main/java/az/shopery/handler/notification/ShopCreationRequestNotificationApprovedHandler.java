package az.shopery.handler.notification;

import az.shopery.model.event.ShopCreationRequestApprovedEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShopCreationRequestNotificationApprovedHandler
        implements NotificationHandler<ShopCreationRequestApprovedEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.SHOP_APPROVED;
    }

    @Override
    public void handle(ShopCreationRequestApprovedEvent event) {
        emailService.sendShopApprovedEmail(
                event.creatorEmail(),
                event.creatorName(),
                event.shopName()
        );

        log.debug("Shop creation approved notification sent to {}", event.creatorEmail());
    }
}

