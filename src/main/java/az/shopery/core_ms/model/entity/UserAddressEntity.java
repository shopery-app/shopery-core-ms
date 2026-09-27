package az.shopery.core_ms.model.entity;

import az.shopery.core_ms.utils.enums.AddressType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    @Builder.Default
    AddressType addressType = AddressType.HOUSE;
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    boolean isDefault = Boolean.FALSE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    UserEntity user;
}
