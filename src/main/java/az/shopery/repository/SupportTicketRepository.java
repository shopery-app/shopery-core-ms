package az.shopery.repository;

import az.shopery.model.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, UUID> {
}
