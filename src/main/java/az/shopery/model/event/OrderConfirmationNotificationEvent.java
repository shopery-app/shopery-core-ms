package az.shopery.model.event;

import az.shopery.model.entity.OrderEntity;
import java.util.List;

public record OrderConfirmationNotificationEvent(
        String userEmail,
        String userName,
        List<OrderEntity> createdOrders) {
}
