package az.shopery.service;

import java.util.List;

public interface EmailService {
    void sendVerificationCode(String to, String name, String code, Boolean isRegistration);
    void sendPasswordResetLink(String to, String name, String token);
    void sendOrderConfirmation(String to, String name, List<String> orderIds);
    void sendPasswordChangedNotification(String to, String name);
    void sendMerchantClosedNotification(String to, String customerName, String merchantName);
    void sendSupportTicketClosedNotification(String to, String userName, String ticketSubject, String ticketId);
    void sendShopApprovedEmail(String to, String userName, String shopName);
    void sendShopRejectedEmail(String to, String userName, String shopName, String rejectionReason);
}
