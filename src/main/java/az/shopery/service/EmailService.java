package az.shopery.service;

public interface EmailService {
    void sendVerificationCode(String to, String name, String code);
    void sendPasswordResetLink(String to, String name, String token);
}
