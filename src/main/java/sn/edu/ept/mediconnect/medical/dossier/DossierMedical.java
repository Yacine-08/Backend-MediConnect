package sn.edu.ept.mediconnect.medical.dossier;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.users.patient.Patient;
import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers_medicaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @Column(name = "antecedents_medicaux", columnDefinition = "TEXT")
    private String antecedentsMedicaux;

    @Column(name = "antecedents_chirurgicaux", columnDefinition = "TEXT")
    private String antecedentsChirurgicaux;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "antecedents_familiaux", columnDefinition = "TEXT")
    private String antecedentsFamiliaux;

    @Column(name = "traitement_en_cours", columnDefinition = "TEXT")
    private String traitementEnCours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut = StatutDossier.ACTIF;

    @Column(name = "date_ouverture", updatable = false)
    private LocalDateTime dateOuverture;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @PrePersist
    protected void onCreate() {
        dateOuverture = LocalDateTime.now();
        dateMiseAJour = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateMiseAJour = LocalDateTime.now();
    }
}