package sn.edu.ept.mediconnect.medical.rendezvous;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.patient.Patient;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hopital_id")
    private Hopital hopital;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRendezVous type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRendezVous statut = StatutRendezVous.PLANIFIE;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "lien_video")
    private String lienVideo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}