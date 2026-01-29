package az.shopery.listener;

import az.shopery.model.event.PasswordResetLinkEvent;
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
public class PasswordResetLinkListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetLinkEvent(PasswordResetLinkEvent event) {
        emailService.sendPasswordResetLink(
                event.userEmail(),
                event.userName(),
                event.token()
        );
    }
}
