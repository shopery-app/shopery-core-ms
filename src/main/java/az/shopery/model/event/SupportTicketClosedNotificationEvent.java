package az.shopery.model.event;

public record SupportTicketClosedNotificationEvent(
        String email,
        String userName,
        String subject,
        String ticketId) {
}
