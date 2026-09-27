package az.shopery.core_ms.repository;

import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.model.entity.task.SupportTicketEntity;
import az.shopery.core_ms.model.entity.task.TaskEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    @Query("SELECT t FROM SupportTicketEntity t WHERE t.createdBy = :user")
    Page<SupportTicketEntity> getAllSupportTicketsByCreatedBy(UserEntity user, Pageable pageable);
    @Query("SELECT t FROM SupportTicketEntity t WHERE t.id = :id AND t.createdBy = :user")
    Optional<SupportTicketEntity> findSupportTicketByIdAndCreatedBy(UUID id, UserEntity user);
}
