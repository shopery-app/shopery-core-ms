package az.shopery.listener;

import az.shopery.model.event.ShopCreationRequestApprovedEvent;
import az.shopery.model.event.ShopCreationRequestRejectedEvent;
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
public class ShopCreationRequestNotificationListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApprove(ShopCreationRequestApprovedEvent event) {
        try {
            emailService.sendShopApprovedEmail(
                    event.creatorEmail(),
                    event.creatorName(),
                    event.shopName()
            );
        } catch (Exception e) {
            log.error("Failed to send shop creation request approved notification to {}", event.creatorEmail(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReject(ShopCreationRequestRejectedEvent event) {
        try {
            emailService.sendShopRejectedEmail(
                    event.creatorEmail(),
                    event.creatorName(),
                    event.shopName(),
                    event.rejectionReason()
            );
        } catch (Exception e) {
            log.error("Failed to send shop creation request rejected notification to {}", event.creatorEmail(), e);
        }
    }
}
