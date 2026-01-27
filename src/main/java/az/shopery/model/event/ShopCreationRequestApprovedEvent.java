package az.shopery.model.event;

public record ShopCreationRequestApprovedEvent(
        String creatorEmail,
        String creatorName,
        String shopName) {
}
