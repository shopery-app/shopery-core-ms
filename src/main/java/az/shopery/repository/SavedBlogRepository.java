package az.shopery.repository;

import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.SavedBlogEntity;
import az.shopery.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SavedBlogRepository extends JpaRepository<SavedBlogEntity, UUID> {
    Optional<SavedBlogEntity> findByBlog(BlogEntity blog);
    Boolean existsByBlogAndUser(BlogEntity blog, UserEntity user);
    void deleteByBlog(BlogEntity blog);
    Page<SavedBlogEntity> findAllByUserIdAndIsArchived(UUID userId, Boolean isArchived, Pageable pageable);
}
