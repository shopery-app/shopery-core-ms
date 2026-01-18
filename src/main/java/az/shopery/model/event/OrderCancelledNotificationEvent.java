package az.shopery.model.event;

public record OrderCancelledNotificationEvent(
        String email,
        String customerName,
        String merchantName) {
}
