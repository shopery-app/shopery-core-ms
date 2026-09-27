package az.shopery.core_ms.repository;

import az.shopery.core_ms.model.entity.WishlistEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, UUID> {
    @Query("SELECT w FROM WishlistEntity w LEFT JOIN FETCH w.products WHERE w.user.id = :userId")
    Optional<WishlistEntity> findByUserIdWithProducts(@Param("userId") UUID userId);
}
