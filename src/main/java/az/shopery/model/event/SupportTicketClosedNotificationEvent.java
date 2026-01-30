package az.shopery.model.event;

public record SupportTicketClosedNotificationEvent(
        String userEmail,
        String userName,
        String subject,
        String ticketId) {
}
