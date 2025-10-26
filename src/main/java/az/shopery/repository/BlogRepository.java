package az.shopery.repository;

import az.shopery.model.entity.BlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<BlogEntity, UUID> {
    List<BlogEntity> getBlogsByUserEmail(String userEmail);
}
