package az.shopery.listener;

import az.shopery.model.dto.event.OrderCancelledNotificationEvent;
import az.shopery.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelledNotification(OrderCancelledNotificationEvent event) {
        try {
            emailService.sendMerchantClosedNotification(
                    event.email(),
                    event.customerName(),
                    event.merchantName()
            );
        } catch (Exception e) {
            log.error("Failed to send cancellation email to {}", event.email(), e);
        }
    }
}
