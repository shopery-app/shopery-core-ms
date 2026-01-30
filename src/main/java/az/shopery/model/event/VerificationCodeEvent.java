package az.shopery.model.event;

public record VerificationCodeEvent(
        String userEmail,
        String userName,
        String code,
        Boolean isRegistration) {
}
