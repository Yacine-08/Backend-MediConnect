package sn.edu.ept.mediconnect.auth.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "otp_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Code à 6 chiffres
    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeOtp type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalOtp canal;

    // Email ou numéro de téléphone destinataire
    @Column(nullable = false, length = 255)
    private String destination;

    // Expiration : 10 minutes après création
    @Column(name = "expire_a", nullable = false)
    private LocalDateTime expireA;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    // Nombre de tentatives de saisie
    @Column(nullable = false)
    @Builder.Default
    private Integer tentatives = 0;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireA);
    }

    public boolean isValid() {
        return !used && !isExpired() && tentatives < 5;
    }

    public void used() {
        this.used = true;
    }

    public void incrementTentatives() {
        this.tentatives++;
    }
}