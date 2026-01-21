package az.shopery.repository;

import az.shopery.model.entity.SavedBlogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SavedBlogRepository extends JpaRepository<SavedBlogEntity, UUID> {
    Optional<SavedBlogEntity> findByBlogIdAndUserId(UUID blogId, UUID userId);
    Page<SavedBlogEntity> findAllByUserId(UUID userId, Pageable pageable);
}
