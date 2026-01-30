package az.shopery.model.event;

import java.util.List;

public record OrderConfirmationNotificationEvent(
        String userEmail,
        String userName,
        List<String> orderIds) {
}
