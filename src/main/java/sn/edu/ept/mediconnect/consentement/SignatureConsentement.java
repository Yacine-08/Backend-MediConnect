package sn.edu.ept.mediconnect.consentement;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.patient.Patient;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "signatures_consentement",
        uniqueConstraints = @UniqueConstraint(
                name  = "uk_signature_patient_consentement",
                columnNames = {"patient_id", "consentement_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureConsentement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consentement_id", nullable = false)
    private Consentement consentement;

    @Column(name = "accepte", nullable = false)
    private Boolean accepte;

    @Column(name = "date_signature_consentement", nullable = false)
    @Builder.Default
    private LocalDateTime dateSignatureConsentement = LocalDateTime.now();

    @Column(name = "date_modification", nullable = false)
    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    // Auteur de la dernière décision : le patient lui-même, un médecin ou un infirmier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifie_par_id", nullable = false)
    private User modifiedBy;

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}