package az.shopery.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "verification_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    UUID id;

    @Column(name = "token", nullable = false)
    String token;
    @Column(name = "expiry_date", nullable = false)
    LocalDateTime expiryDate;
    @Column(name = "user_name", nullable = false)
    String userName;
    @Column(name = "user_email", nullable = false, unique = true)
    String userEmail;
    @Column(name = "user_password", nullable = false)
    String userPassword;
    @Column(name = "user_phone", nullable = false, unique = true)
    String userPhone;
}
