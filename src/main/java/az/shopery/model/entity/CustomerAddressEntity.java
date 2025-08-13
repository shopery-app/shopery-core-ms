package az.shopery.model.entity;

import az.shopery.utils.enums.AddressType;
import jakarta.persistence.*;

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
@Table(name = "customer_addresses")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerAddressEntity {
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
    @Builder.Default
    @Column(name = "address_type")
    @Enumerated(EnumType.STRING)
    AddressType addressType = AddressType.HOUSE;
    @Builder.Default
    @Column(name = "is_default")
    boolean isDefault = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    CustomerEntity customerEntity;
}
