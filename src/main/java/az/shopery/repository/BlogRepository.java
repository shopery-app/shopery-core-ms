package az.shopery.repository;

import az.shopery.model.entity.BlogEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<BlogEntity, UUID> {
    Optional<BlogEntity> findBlogByIdAndUserEmail(UUID blogId, String userEmail);
    Optional<BlogEntity> findByIdAndUserEmailAndIsArchived(UUID blogId, String userEmail, Boolean isArchived);
    Optional<BlogEntity> findByIdAndIsArchived(UUID blogId, Boolean isArchived);
    Page<BlogEntity> findAllByUserEmailAndIsArchived(String userEmail, Boolean isArchived, Pageable pageable);
    Page<BlogEntity> findAllByIsArchived(Boolean isArchived, Pageable pageable);

    @Query(value = """
        SELECT b FROM BlogEntity b
        JOIN FETCH b.user u
        WHERE b.isArchived = false
          AND (b.blogTitle ILIKE %:query% OR b.content ILIKE %:query%)
        """, countQuery = """
        SELECT COUNT(b) FROM BlogEntity b
        WHERE b.isArchived = false
            AND (b.blogTitle ILIKE %:query% OR b.content ILIKE %:query%)
    """)
    Page<BlogEntity> searchBlogs(@Param("query") String query, Pageable pageable);
}
