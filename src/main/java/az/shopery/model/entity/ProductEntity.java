package az.shopery.model.entity;

import az.shopery.utils.enums.ProductCategory;
import az.shopery.utils.enums.ProductCondition;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "shop_id", nullable = false)
    ShopEntity shop;
    @Column(name = "product_name", nullable = false)
    String productName;
    @Column(name = "description", length = 2000)
    String description;
    @Column(name = "current_price", precision = 10, scale = 2)
    BigDecimal currentPrice;
    @Column(name = "image_url")
    String imageUrl;
    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity;
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    ProductCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    ProductCondition condition;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<PriceHistoryEntity> priceHistory;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
