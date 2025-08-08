package az.shopery.service.impl;

import az.shopery.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

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
}
