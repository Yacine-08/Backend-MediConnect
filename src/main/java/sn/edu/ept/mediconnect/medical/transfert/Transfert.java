package sn.edu.ept.mediconnect.medical.transfert;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.patient.Patient;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    // Médecin destinataire du transfert (celui qui devra l'accepter/refuser)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_destination_id")
    private Medecin medecinDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hopital_source_id")
    private Hopital hopitalSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hopital_destination_id", nullable = false)
    private Hopital hopitalDestination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransfert type;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    private String compteRendu;

    @Column(name = "date_transfert")
    private LocalDateTime dateTransfert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransfert statut = StatutTransfert.EN_ATTENTE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        dateTransfert = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}