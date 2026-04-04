package az.shopery.model.entity;

import az.shopery.utils.enums.SubscriptionTier;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
    @Column(name = "name", nullable = false, length = 40)
    String name;
    @Column(name = "password", nullable = false)
    String password;
    @Column(name = "email", nullable = false, unique = true)
    String email;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    int failedLoginAttempts = 0;
    @Column(name = "account_locked_until")
    LocalDateTime accountLockedUntil;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "user_role", nullable = false)
    UserRole userRole = UserRole.CUSTOMER;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "subscription_tier")
    SubscriptionTier subscriptionTier = SubscriptionTier.NONE;
    @Column(name = "profile_photo_url")
    String profilePhotoUrl;
    @Column(name = "phone")
    String phone;
    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;
    @Column(name = "last_role_change_at")
    Instant lastRoleChangeAt;
    @Column(name = "password_changed_at")
    Instant passwordChangedAt;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    UserStatus status =  UserStatus.ACTIVE;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    WishlistEntity wishlist;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    CartEntity cart;
    @OneToOne(mappedBy = "user", cascade =  CascadeType.ALL, orphanRemoval = true)
    ShopEntity shop;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BlogEntity> blogs;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BlogLikeEntity> blogLikes;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserAddressEntity> userAddresses;
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    List<OrderEntity> orders;
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    List<SavedBlogEntity> savedBlogs;
}
