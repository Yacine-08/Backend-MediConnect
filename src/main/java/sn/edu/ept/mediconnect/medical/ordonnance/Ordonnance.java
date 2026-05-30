package sn.edu.ept.mediconnect.medical.ordonnance;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordonnances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Column(name = "signature_numerique")
    private Boolean signatureNumerique = false;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @OneToMany(mappedBy = "ordonnance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LignePrescription> lignes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        dateEmission = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}