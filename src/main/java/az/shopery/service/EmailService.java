package az.shopery.service;

public interface EmailService {
    void sendVerificationCode(String to, String name, String code);
}
