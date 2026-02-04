package az.shopery.service.impl;

import az.shopery.model.event.NotificationEvent;
import az.shopery.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
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

    @Value("${app.mail.from}")
    private String from;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void sendNotification(NotificationEvent notificationEvent) {
        try {
            Map<String, Object> variables = new HashMap<>(notificationEvent.params());
            variables.put("frontendBaseUrl", frontendBaseUrl);

            if (variables.containsKey("token")) {
                variables.put("resetUrl", frontendBaseUrl + "/reset-password?token=" + variables.get("token"));
            }

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(notificationEvent.type().getTemplateName(), context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(notificationEvent.to());
            helper.setSubject(notificationEvent.type().getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email [{}] successfully sent to {}", notificationEvent.type(), notificationEvent.to());

        } catch (Exception e) {
            log.error("Failed to send email [{}] to {}", notificationEvent.type(), notificationEvent.to(), e);
        }
    }
}
