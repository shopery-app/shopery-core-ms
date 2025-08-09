package az.shopery.repository;

import az.shopery.model.entity.VerificationTokenEntity;
import az.shopery.utils.enums.VerificationProgress;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByUserEmail(String email);
    Optional<VerificationTokenEntity> findByUserEmailAndProgress(String email, VerificationProgress progress);
    void deleteByProgressOrExpiryDateBefore(VerificationProgress progress, LocalDateTime expiryDate);
}
