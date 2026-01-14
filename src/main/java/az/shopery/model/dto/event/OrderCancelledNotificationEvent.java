package az.shopery.model.dto.event;

public record OrderCancelledNotificationEvent(
        String email,
        String customerName,
        String merchantName) {
}
