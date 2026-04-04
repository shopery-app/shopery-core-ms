package az.shopery.repository;

import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.utils.enums.ShopStatus;
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
    Boolean existsByUserAndStatus(UserEntity userEntity, ShopStatus shopStatus);
    Boolean existsByShopName(String shopName);
    Optional<ShopEntity> findByUserEmailAndStatus(String userEmail, ShopStatus shopStatus);
    Optional<ShopEntity> findByUser(UserEntity userEntity);

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
}
