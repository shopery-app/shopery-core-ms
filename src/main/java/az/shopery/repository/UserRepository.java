package az.shopery.repository;

import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.SubscriptionTier;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<UserEntity> findAllByUserRoleAndStatus(UserRole userRole, UserStatus status, Pageable pageable);
    List<UserEntity> findAllByUserRoleAndStatus(UserRole userRole, UserStatus status);
    Optional<UserEntity> findByEmailAndUserRoleAndStatusAndSubscriptionTier(String email, UserRole userRole, UserStatus status, SubscriptionTier subscriptionTier);
}
