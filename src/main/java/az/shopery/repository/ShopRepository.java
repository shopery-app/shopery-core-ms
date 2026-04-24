package az.shopery.repository;

import az.shopery.model.dto.projection.AdminShopProjection;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.ShopStatus;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, UUID> {
    Boolean existsByUserAndStatusIn(UserEntity userEntity, Collection<ShopStatus> shopStatuses);
    Boolean existsByShopName(String shopName);
    Optional<ShopEntity> findByUserEmailAndStatus(String userEmail, ShopStatus shopStatus);
    Optional<ShopEntity> findByUserAndStatus(UserEntity userEntity, ShopStatus shopStatus);
    Optional<ShopEntity> findByShopName(String shopName);

    @Query("""
        SELECT s
        FROM ShopEntity s
        WHERE s.user.status = 'ACTIVE' AND s.status = 'ACTIVE'
    """)
    Page<ShopEntity> findAllWithActiveOwners(Pageable pageable);

    @Query("""
        SELECT s
        FROM ShopEntity s
        LEFT JOIN FETCH s.products
        WHERE s.id = :id
          AND s.user.status = 'ACTIVE' AND s.status = 'ACTIVE'
    """)
    Optional<ShopEntity> findActiveShopByIdWithProducts(@Param("id") UUID id);

    @Query("""
        SELECT s
        FROM ShopEntity s
        LEFT JOIN FETCH s.products
        WHERE s.shopName = :shopName
          AND s.user.status = 'ACTIVE' AND s.status = 'ACTIVE'
    """)
    Optional<ShopEntity> findActiveShopByShopNameWithProducts(@Param("shopName") String shopName);

    @Query("""
        SELECT
            s.id AS id,
            s.shopName AS shopName,
            s.description AS description,
            s.totalIncome AS totalIncome,
            s.rating AS rating,
            s.createdAt AS createdAt,
            COUNT(p.id) AS totalProducts,
            u.subscriptionTier AS subscriptionTier,
            s.status AS shopStatus,
            u.email AS userEmail,
            u.status AS userStatus
        FROM ShopEntity s
        LEFT JOIN s.user u
        LEFT JOIN s.products p
        GROUP BY
            s.id, s.shopName, s.description, s.totalIncome, s.rating, s.createdAt,
            u.subscriptionTier, s.status, u.email, u.status
    """)
    Page<AdminShopProjection> findAllWithProductCount(Pageable pageable);
}
