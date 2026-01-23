package az.shopery.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@FieldDefaults(level= AccessLevel.PRIVATE)
@Table(name = "blog_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "blog_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    UserEntity user;
    @ManyToOne
    @JoinColumn(name = "blog_id", nullable = false)
    BlogEntity blog;
    @CreatedDate
    @Column(name = "liked_at", nullable = false, updatable = false)
    Instant likedAt;
}
