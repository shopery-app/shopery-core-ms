package az.shopery.service.impl;

import az.shopery.model.entity.OrderEntity;
import az.shopery.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void sendVerificationCode(String to, String name, String code, Boolean isRegistration) {
        sendEmail(
                to,
                "Your Shopery Verification Code",
                "verification-email",
                Map.of(
                        "userName", name,
                        "verificationCode", code,
                        "isRegistration", isRegistration
                )
        );
    }

    @Override
    public void sendPasswordResetLink(String to, String name, String token) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;

        sendEmail(
                to,
                "Your Shopery Password Reset Link",
                "password-reset-email",
                Map.of(
                        "userName", name,
                        "resetUrl", resetUrl
                )
        );
    }

    @Override
    public void sendOrderConfirmation(String to, String name, List<OrderEntity> orders) {
        sendEmail(
                to,
                "Your Shopery Order Confirmation",
                "order-confirmation-email",
                Map.of(
                        "userName", name,
                        "orders", orders
                )
        );
    }

    @Override
    public void sendPasswordChangedNotification(String to, String name) {
        sendEmail(
                to,
                "Shopery Password Update",
                "change-password-email",
                Map.of("userName", name)
        );
    }

    @Override
    public void sendMerchantClosedNotification(String to, String customerName, String merchantName) {
        sendEmail(
                to,
                "Shopery Merchant Update",
                "merchant-closed-notification-email",
                Map.of(
                        "userName", customerName,
                        "merchantName", merchantName
                )
        );
    }

    private void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email [{}] successfully sent to {}", subject, to);

        } catch (Exception e) {
            log.error("Failed to send email [{}] to {}", subject, to, e);
        }
    }
}
