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
        emailService.sendShopApprovedEmail(
                event.creatorEmail(),
                event.creatorName(),
                event.shopName()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReject(ShopCreationRequestRejectedEvent event) {
        emailService.sendShopRejectedEmail(
                event.creatorEmail(),
                event.creatorName(),
                event.shopName(),
                event.rejectionReason()
        );
    }
}
