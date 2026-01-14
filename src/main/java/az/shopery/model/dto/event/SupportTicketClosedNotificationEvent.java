package az.shopery.model.dto.event;

public record SupportTicketClosedNotificationEvent(
        String email,
        String userName,
        String subject,
        String ticketId) {
}
