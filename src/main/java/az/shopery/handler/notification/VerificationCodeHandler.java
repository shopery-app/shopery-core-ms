package az.shopery.handler.notification;

import az.shopery.model.event.VerificationCodeEvent;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeHandler
        implements NotificationHandler<VerificationCodeEvent> {

    private final EmailService emailService;

    @Override
    public NotificationType supports() {
        return NotificationType.VERIFICATION_CODE;
    }

    @Override
    public void handle(VerificationCodeEvent payload) {
        emailService.sendVerificationCode(
                payload.userEmail(),
                payload.userName(),
                payload.code(),
                payload.isRegistration()
        );

        log.debug("Verification code notification sent to {}", payload.userEmail());
    }
}

