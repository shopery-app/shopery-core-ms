package az.shopery.repository;

import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, UUID> {
    @Query("select st from SupportTicketEntity st left join fetch st.createdBy where st.assignedAdmin = :admin")
    Page<SupportTicketEntity> getAllSupportTicketsByAssignedAdmin(@Param("admin") UserEntity admin, Pageable pageable);
    @Query("select st from SupportTicketEntity st left join fetch st.assignedAdmin where st.createdBy = :creator")
    Page<SupportTicketEntity> getAllSupportTicketsByCreatedBy(@Param("creator") UserEntity creator, Pageable pageable);
    @Query("select st from SupportTicketEntity st left join fetch st.assignedAdmin where st.id = :id and st.createdBy = :user")
    Optional<SupportTicketEntity> findByIdAndCreatedBy(@Param("id") UUID id, @Param("user") UserEntity user);
    @Query("select st from SupportTicketEntity st left join fetch st.createdBy where st.id = :id and st.assignedAdmin = :admin")
    Optional<SupportTicketEntity> findByIdAndAssignedAdmin(@Param("id") UUID id, @Param("admin") UserEntity admin);
}
