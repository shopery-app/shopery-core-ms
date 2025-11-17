package az.shopery.service.impl;

import az.shopery.model.entity.OrderEntity;
import az.shopery.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void sendVerificationCode(String to, String name, String code) {
        try {
            Context context = new Context();
            context.setVariable("userName", name);
            context.setVariable("verificationCode", code);

            String htmlContent = templateEngine.process("verification-email", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Shopery Verification Code");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Verification code sent to {}", to);
        } catch (Exception e) {
            log.error("Error sending verification code to {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetLink(String to, String name, String token) {
        try {
            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("userName", name);
            context.setVariable("resetUrl", resetUrl);

            String htmlContent = templateEngine.process("password-reset-email", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Shopery Password Reset Link");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Password reset link sent to {}", to);
        } catch (Exception e) {
            log.error("Error sending password reset link to {}", to, e);
        }
    }

    @Override
    public void sendOrderConfirmation(String to, String name, List<OrderEntity> orders) {
        try {
            Context context = new Context();
            context.setVariable("userName", name);
            context.setVariable("orders", orders);

            String htmlContent = templateEngine.process("order-confirmation-email", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Shopery Order Confirmation");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Order confirmation email sent to {}", to);
        } catch (Exception e) {
            log.error("Error sending order confirmation to {}", to, e);
        }
    }
}
