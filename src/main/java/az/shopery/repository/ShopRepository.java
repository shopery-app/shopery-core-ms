package az.shopery.repository;

import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<ShopEntity, UUID> {
    boolean existsByUser(UserEntity userEntity);
    boolean existsByShopName(String shopName);
}
