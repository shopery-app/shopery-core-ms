package az.shopery.core_ms.repository;

import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
import az.shopery.core_ms.utils.enums.UserRole;
import az.shopery.core_ms.utils.enums.UserStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndStatus(String email, UserStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findAndLockByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<UserEntity> findByEmailAndUserRoleAndStatus(String email, UserRole userRole, UserStatus status);
    Optional<UserEntity> findByEmailAndUserRoleAndStatusAndSubscriptionTier(String email, UserRole userRole, UserStatus status, SubscriptionTier subscriptionTier);
    @Query(value = "SELECT * FROM users WHERE user_role = 'ADMIN' AND status = 'ACTIVE' ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<UserEntity> findRandomActiveAdmin();
}
