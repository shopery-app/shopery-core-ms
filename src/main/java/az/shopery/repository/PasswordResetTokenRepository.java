package az.shopery.repository;

import az.shopery.model.entity.PasswordResetTokenEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional<PasswordResetTokenEntity> findByToken(String token);
    Optional<PasswordResetTokenEntity> findByUserEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime now);
}
