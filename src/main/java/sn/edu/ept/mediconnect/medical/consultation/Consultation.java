package sn.edu.ept.mediconnect.medical.consultation;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.medical.rendezvous.RendezVous;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infirmier_id")
    private Infirmier infirmier;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id")
    private RendezVous rendezVous;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    // Phase 1 — Constantes vitales (Infirmier)
    @Column(name = "tension_arterielle", length = 20)
    private String tensionArterielle;

    @Column(name = "frequence_cardiaque")
    private Integer frequenceCardiaque;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "poids")
    private Float poids;

    @Column(name = "taille")
    private Float taille;

    @Column(name = "spo2")
    private Float spo2;

    // Phase 2 — Informations cliniques (Médecin / Cardiologue)
    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String anamnese;

    @Column(name = "examen_clinique", columnDefinition = "TEXT")
    private String examenClinique;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(name = "analyse_ia", columnDefinition = "TEXT")
    private String analyseIa;

    @Column(name = "score_confiance")
    private Float scoreConfiance;

    @Column(name = "pathologies_detectees", columnDefinition = "TEXT")
    private String pathologiesDetectees;

    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    private String compteRendu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConsultation statut = StatutConsultation.EN_ATTENTE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dateHeure == null) dateHeure = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}