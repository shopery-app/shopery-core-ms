package az.shopery.model.event;

public record PasswordResetLinkEvent(
        String userEmail,
        String userName,
        String token) {
}
