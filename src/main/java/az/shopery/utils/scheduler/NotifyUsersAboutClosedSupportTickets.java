package az.shopery.utils.scheduler;

import static az.shopery.utils.common.NameMapperHelper.first;

import az.shopery.model.dto.event.SupportTicketClosedNotificationEvent;
import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.repository.admin.SupportTicketRepository;
import az.shopery.utils.enums.TicketStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyUsersAboutClosedSupportTickets {

    private final SupportTicketRepository supportTicketRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyUsersAboutClosedSupportTickets() {
        List<SupportTicketEntity> tickets = supportTicketRepository.findAllByStatusAndIsUserNotifiedFalse(TicketStatus.CLOSED);
        for (SupportTicketEntity ticket : tickets) {
            ticket.setIsUserNotified(Boolean.TRUE);
            applicationEventPublisher.publishEvent(new SupportTicketClosedNotificationEvent(
                    ticket.getCreatedBy().getEmail(),
                    first(ticket.getCreatedBy().getName()),
                    ticket.getSubject(),
                    ticket.getId().toString()
            ));
        }
        log.info("Marked {} support tickets as notified", tickets.size());
    }
}
