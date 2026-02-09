package az.shopery.repository;

import az.shopery.model.entity.ProductEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findByShopId(UUID shopId, Pageable pageable);
    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.priceHistory WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithPriceHistory(@Param("id") UUID id);
    @Query("SELECT p FROM ProductEntity p WHERE p.originalPrice IS NOT NULL AND p.originalPrice > 0 AND p.currentPrice < p.originalPrice ORDER BY ((p.originalPrice - p.currentPrice) / p.originalPrice) DESC")
    Page<ProductEntity> findTopDiscountedProducts(Pageable pageable);
    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.shop WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithShop(@Param("id") UUID id);
    boolean existsByIdAndShopUser(UUID productId, UserEntity user);

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE (:category IS NULL OR p.category = :category)
        AND (:condition IS NULL OR p.condition = :condition)
        AND (:minPrice IS NULL OR p.currentPrice >= :minPrice)
        AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)
    """)
    Page<ProductEntity> searchPublicProducts(
            @Param("category") ProductCategory category,
            @Param("condition") ProductCondition condition,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );
}
