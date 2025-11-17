package az.shopery.model.entity;

import az.shopery.utils.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    ShopEntity shop;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    OrderStatus status = OrderStatus.PLACED;
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    BigDecimal totalPrice;
    @Column(name = "address_line1", nullable = false)
    String addressLine1;
    @Column(name = "address_line2")
    String addressLine2;
    @Column(name = "city", nullable = false)
    String city;
    @Column(name = "country", nullable = false)
    String country;
    @Column(name = "postal_code", nullable = false)
    String postalCode;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItemEntity> items;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
