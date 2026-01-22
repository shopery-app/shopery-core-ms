package az.shopery.repository;

import az.shopery.model.entity.BlogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<BlogEntity, UUID> {
    Optional<BlogEntity> findBlogByIdAndUserEmail(UUID blogId, String userEmail);
    Optional<BlogEntity> findByIdAndUserEmailAndIsArchived(UUID blogId, String userEmail, Boolean isArchived);
    Page<BlogEntity> findAllByUserEmailAndIsArchived(String userEmail, Boolean isArchived, Pageable pageable);
    Page<BlogEntity> findAllByIsArchived(Boolean isArchived, Pageable pageable);
}
