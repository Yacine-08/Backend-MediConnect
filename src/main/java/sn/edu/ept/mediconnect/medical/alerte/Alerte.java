package sn.edu.ept.mediconnect.medical.alerte;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import sn.edu.ept.mediconnect.users.patient.Patient;
import java.time.LocalDateTime;


@Entity
@Table(name = "alertes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauAlerte niveau;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 200)
    private String source;

    @Column(nullable = false)
    private Boolean acquittee = false;

    @Column(name = "date_emission", updatable = false)
    private LocalDateTime dateEmission;

    @Column(name = "date_acquittement")
    private LocalDateTime dateAcquittement;

    @PrePersist
    protected void onCreate() {
        dateEmission = LocalDateTime.now();
    }
}