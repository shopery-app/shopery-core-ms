package az.shopery.utils.scheduler;

import az.shopery.model.entity.OrderEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.OrderRepository;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.OrderStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyUsersAboutCancelledOrders {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyUsersAboutCancelledOrders() {
        List<OrderEntity> orders = orderRepository.findAllByStatusAndIsUserNotifiedFalse(OrderStatus.CANCELLED);
        for (OrderEntity order : orders) {
            order.setIsUserNotified(Boolean.TRUE);
            applicationEventPublisher.publishEvent(new NotificationEvent(
                    order.getUser().getEmail(),
                    NotificationType.ORDER_CANCELLED,
                    Map.of(
                            "userName", order.getUser().getName(),
                            "merchantName", order.getShop().getUser().getName()
                    )
            ));
        }
        log.info("Marked {} orders as notified", orders.size());
    }
}
