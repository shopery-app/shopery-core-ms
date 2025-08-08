package az.shopery.repository;

import az.shopery.model.entity.VerificationTokenEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByUserEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime now);
}
