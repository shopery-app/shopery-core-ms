package az.shopery.repository.admin;

import az.shopery.model.entity.ShopCreationRequestEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopCreationRequestRepository extends JpaRepository<ShopCreationRequestEntity, UUID> {
}
