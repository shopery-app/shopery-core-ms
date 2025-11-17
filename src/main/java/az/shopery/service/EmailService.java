package az.shopery.service;

import az.shopery.model.entity.OrderEntity;
import java.util.List;

public interface EmailService {
    void sendVerificationCode(String to, String name, String code);
    void sendPasswordResetLink(String to, String name, String token);
    void sendOrderConfirmation(String to, String name, List<OrderEntity> orders);
}
