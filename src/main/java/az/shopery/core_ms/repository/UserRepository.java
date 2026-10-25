package az.shopery.core_ms.repository;

import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
import az.shopery.core_ms.utils.enums.UserRole;
import az.shopery.core_ms.utils.enums.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndStatus(String email, UserStatus status);
    Boolean existsByEmail(String email);
    Optional<UserEntity> findByEmailAndUserRoleAndStatus(String email, UserRole userRole, UserStatus status);
    Optional<UserEntity> findByEmailAndUserRoleAndStatusAndSubscriptionTier(String email, UserRole userRole, UserStatus status, SubscriptionTier subscriptionTier);
}
