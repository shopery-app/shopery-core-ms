package az.shopery.repository;

import az.shopery.model.entity.BlogEntity;
import az.shopery.model.entity.BlogLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BlogLikeRepository extends JpaRepository<BlogLikeEntity, UUID> {
    void deleteByUserEmailAndBlog(String userEmail, BlogEntity blog);
    boolean existsByUserEmailAndBlog(String userEmail, BlogEntity blog);
    Integer countByBlog(BlogEntity blog);
}
