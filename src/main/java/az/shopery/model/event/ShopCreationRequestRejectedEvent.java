package az.shopery.model.event;

public record ShopCreationRequestRejectedEvent(
        String creatorEmail,
        String creatorName,
        String shopName,
        String rejectionReason) {
}
