package az.shopery.utils.scheduler;

import static az.shopery.utils.common.NameMapperHelper.first;

import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.TaskRepository;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.TicketStatus;
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
public class NotifyUsersAboutClosedSupportTickets {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyUsersAboutClosedSupportTickets() {
        List<SupportTicketEntity> tickets = taskRepository.findAllByTicketStatusAndIsUserNotifiedFalse(TicketStatus.CLOSED);
        for (SupportTicketEntity ticket : tickets) {
            ticket.setIsUserNotified(Boolean.TRUE);
            applicationEventPublisher.publishEvent(new NotificationEvent(
                    ticket.getCreatedBy().getEmail(),
                    NotificationType.SUPPORT_TICKET_CLOSED,
                    Map.of()
//                    first(ticket.getCreatedBy().getName()),
//                    ticket.getSubject(),
//                    ticket.getId().toString()
            ));
        }
        log.info("Marked {} support tickets as notified", tickets.size());
    }
}
