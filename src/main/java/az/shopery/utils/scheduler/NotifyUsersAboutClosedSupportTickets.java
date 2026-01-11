package az.shopery.utils.scheduler;

import static az.shopery.utils.common.NameMapperHelper.first;

import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.repository.SupportTicketRepository;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.TicketStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyUsersAboutClosedSupportTickets {

    private final SupportTicketRepository supportTicketRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void notifyUsersAboutClosedSupportTickets() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running job to send mails to users about their closed support tickets {}", now);
        List<SupportTicketEntity> tickets = supportTicketRepository.findAllByStatusAndIsUserNotifiedFalse(TicketStatus.CLOSED);
        for (SupportTicketEntity ticket : tickets) {
            emailService.sendSupportTicketClosedNotification(
                    ticket.getCreatedBy().getEmail(),
                    first(ticket.getCreatedBy().getName()),
                    ticket.getSubject(),
                    ticket.getId().toString());
            ticket.setIsUserNotified(Boolean.TRUE);
        }
        log.info("Sending users about their closed support tickets finished at {}", now);
    }
}
