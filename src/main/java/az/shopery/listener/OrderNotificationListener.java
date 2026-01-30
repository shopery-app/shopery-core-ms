package az.shopery.listener;

import az.shopery.model.event.OrderCancelledNotificationEvent;
import az.shopery.model.event.OrderConfirmationNotificationEvent;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmationNotification(OrderConfirmationNotificationEvent event) {
        try {
            emailService.sendOrderConfirmation(
                    event.userEmail(),
                    event.userName(),
                    event.createdOrders()
            );
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}", event.userEmail(), e);
        }
    }
}
