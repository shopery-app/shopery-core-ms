package az.shopery.repository;

import az.shopery.model.entity.OrderEntity;
import java.util.List;
import java.util.UUID;
import az.shopery.model.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    @EntityGraph(attributePaths = {"items", "shop"})
    List<OrderEntity> findAllByUserOrderByCreatedAtDesc(UserEntity userEntity);
}