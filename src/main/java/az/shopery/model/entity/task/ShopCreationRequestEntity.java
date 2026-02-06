package az.shopery.model.entity.task;

import az.shopery.utils.enums.RequestStatus;
import az.shopery.utils.enums.SubscriptionTier;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("SHOP_CREATION_REQUEST")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopCreationRequestEntity extends TaskEntity {
    @Column(name = "shop_name", nullable = false)
    String shopName;
    @Column(name = "description")
    String description;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "subscription_tier")
    SubscriptionTier subscriptionTier = SubscriptionTier.BASIC;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "request_status", nullable = false)
    RequestStatus requestStatus = RequestStatus.PENDING;
    @Column(name = "rejection_reason")
    String rejectionReason;
}
