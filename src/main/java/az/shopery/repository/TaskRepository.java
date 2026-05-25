package az.shopery.repository;

import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.entity.task.TaskEntity;
import az.shopery.utils.enums.TaskCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    @EntityGraph(attributePaths = {"createdBy", "assignedAdmin"})
    Page<TaskEntity> findAllByAssignedAdmin(UserEntity assignedAdmin, Pageable pageable);
    @EntityGraph(attributePaths = {"createdBy", "assignedAdmin"})
    Page<TaskEntity> findAllByAssignedAdminAndTaskCategory(UserEntity assignedAdmin, TaskCategory taskCategory, Pageable pageable);
    Optional<TaskEntity> findByIdAndAssignedAdmin(UUID id, UserEntity assignedAdmin);
    @Query("SELECT t FROM SupportTicketEntity t WHERE t.createdBy = :user")
    Page<SupportTicketEntity> getAllSupportTicketsByCreatedBy(UserEntity user, Pageable pageable);
    @Query("SELECT t FROM SupportTicketEntity t WHERE t.id = :id AND t.createdBy = :user")
    Optional<SupportTicketEntity> findSupportTicketByIdAndCreatedBy(UUID id, UserEntity user);
}
