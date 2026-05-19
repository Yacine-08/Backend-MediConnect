package sn.edu.ept.mediconnect.consentement;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.users.patient.Patient;

import java.time.LocalDateTime;

@Entity
@Table(name = "signatures_consentement")
@Getter
@Setter
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

    @Column(name = "date_signature_consentement", nullable = false)
    @Builder.Default
    private LocalDateTime dateSignatureConsentement = LocalDateTime.now();
}