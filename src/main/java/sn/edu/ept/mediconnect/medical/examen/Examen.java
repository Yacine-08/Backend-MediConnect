package sn.edu.ept.mediconnect.medical.examen;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import java.time.LocalDateTime;


@Entity
@Table(name = "examens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(nullable = false, length = 200)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamenType type;

    @Column(name = "fichier_url")
    private String fichierUrl;

    @Column(length = 50)
    private String format;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutExamen statut = StatutExamen.EN_ATTENTE;

    @Column(name = "date_acquisition")
    private LocalDateTime dateAcquisition;

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