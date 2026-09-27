package az.shopery.core_ms.repository;

import az.shopery.core_ms.model.entity.ShopRatingEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRatingRepository extends JpaRepository<ShopRatingEntity, UUID> {
    Optional<ShopRatingEntity> findByUserIdAndShopId(UUID userId, UUID shopId);
    @Query("SELECT AVG(r.rating) FROM ShopRatingEntity r WHERE r.shop.id = :shopId")
    Double calculateAverageRating(UUID shopId);
}
