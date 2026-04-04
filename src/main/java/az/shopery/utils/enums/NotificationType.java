package az.shopery.utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    VERIFICATION_CODE("verification-email", "Your Shopery Verification Code"),
    PASSWORD_RESET_LINK("password-reset-email", "Your Shopery Password Reset Link"),
    ORDER_CONFIRMED("order-confirmation-email", "Your Shopery Order Confirmation"),
    PASSWORD_CHANGED("change-password-email", "Shopery Password Update"),
    ORDER_CANCELLED("shop-owner-closed-notification-email", "Shopery Shop Owner Update"),
    SUPPORT_TICKET_CLOSED("support-ticket-closed-email", "Your Support Ticket Has Been Closed"),
    SHOP_APPROVED("shop-creation-approved-email", "Your Shop Creation Request Has Been Approved"),
    SHOP_REJECTED("shop-creation-rejected-email", "Your Shop Creation Request Has Been Rejected");

    private final String templateName;
    private final String subject;
}
