package az.shopery.utils.scheduler;

import static az.shopery.utils.common.NameMapperHelper.first;

import az.shopery.model.dto.event.OrderCancelledNotificationEvent;
import az.shopery.model.entity.OrderEntity;
import az.shopery.repository.OrderRepository;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.OrderStatus;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
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
            applicationEventPublisher.publishEvent(new OrderCancelledNotificationEvent(
                    order.getUser().getEmail(),
                    first(order.getUser().getName()),
                    order.getShop().getUser().getName()
            ));
        }
        log.info("Marked {} orders as notified", orders.size());
    }
}
