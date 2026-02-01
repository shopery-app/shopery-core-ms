package az.shopery.listener;

import az.shopery.model.event.VerificationCodeEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendVerificationCode(VerificationCodeEvent event) {
        emailService.sendVerificationCode(
                event.userEmail(),
                event.userName(),
                event.code(),
                event.isRegistration()
        );
    }
}
