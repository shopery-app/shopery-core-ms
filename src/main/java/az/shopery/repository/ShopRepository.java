package az.shopery.repository;

import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
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
    Boolean existsByUser(UserEntity userEntity);
    Boolean existsByShopName(String shopName);
    Optional<ShopEntity> findByUserEmail(String userEmail);
    Optional<ShopEntity> findByUser(UserEntity userEntity);

    @Query("""
        SELECT s
        FROM ShopEntity s
        WHERE s.user.status = "ACTIVE"
    """)
    Page<ShopEntity> findAllWithActiveOwners(Pageable pageable);

    @Query("""
        SELECT s
        FROM ShopEntity s
        LEFT JOIN FETCH s.products
        WHERE s.id = :id
          AND s.user.status = "ACTIVE"
    """)
    Optional<ShopEntity> findActiveShopByIdWithProducts(@Param("id") UUID id);

    @Query("""
        SELECT s
        FROM ShopEntity s
        LEFT JOIN FETCH s.products
        WHERE s.shopName = :shopName
          AND s.user.status = "ACTIVE"
    """)
    Optional<ShopEntity> findActiveShopByShopNameWithProducts(@Param("shopName") String shopName);
}
