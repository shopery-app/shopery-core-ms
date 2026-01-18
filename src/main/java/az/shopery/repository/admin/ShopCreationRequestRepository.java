package az.shopery.repository.admin;

import az.shopery.model.entity.ShopCreationRequestEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.RequestStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopCreationRequestRepository extends JpaRepository<ShopCreationRequestEntity, UUID> {
    @Query("select scr from ShopCreationRequestEntity scr left join fetch scr.createdBy where scr.id = :id and scr.assignedAdmin = :admin and scr.status = :status")
    Optional<ShopCreationRequestEntity> findByIdAndAssignedAdminAndStatus(@Param("id") UUID id, @Param("admin") UserEntity admin, @Param("status") RequestStatus status);
    Page<ShopCreationRequestEntity> findByAssignedAdminAndStatus(UserEntity admin, RequestStatus status, Pageable pageable);
}
