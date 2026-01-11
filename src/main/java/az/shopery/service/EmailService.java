package az.shopery.service;

import az.shopery.model.entity.OrderEntity;
import java.util.List;

public interface EmailService {
    void sendVerificationCode(String to, String name, String code, Boolean isRegistration);
    void sendPasswordResetLink(String to, String name, String token);
    void sendOrderConfirmation(String to, String name, List<OrderEntity> orders);
    void sendPasswordChangedNotification(String to, String name);
    void sendMerchantClosedNotification(String to, String customerName, String merchantName);
    void sendSupportTicketClosedNotification(String to, String userName, String ticketSubject, String ticketId);
}
