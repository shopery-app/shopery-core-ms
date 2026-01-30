package az.shopery.model.event;

public record PasswordChangedNotificationEvent(
        String userEmail,
        String userName) {
}
