package az.shopery.model.entity;

import az.shopery.utils.enums.ShopStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "shops")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    UserEntity user;
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductEntity> products;
    @Column(name = "shop_name", nullable = false, unique = true, length = 40)
    String shopName;
    @Column(name = "description", length = 2000)
    String description;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
    @Column(name = "total_income")
    BigDecimal totalIncome;
    @Column(name = "rating")
    Double rating;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    ShopStatus status = ShopStatus.PENDING;
}
