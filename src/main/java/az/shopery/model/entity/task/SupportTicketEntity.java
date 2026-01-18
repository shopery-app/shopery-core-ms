package az.shopery.model.entity.task;

import az.shopery.utils.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("SUPPORT_TICKET")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportTicketEntity extends TaskEntity{
    @Column(name = "subject", nullable = false)
    String subject;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    String description;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "ticket_status", nullable = false)
    TicketStatus ticketStatus = TicketStatus.OPEN;
}
