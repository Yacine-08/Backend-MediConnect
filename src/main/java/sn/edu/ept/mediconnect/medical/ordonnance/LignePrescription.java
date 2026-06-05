package sn.edu.ept.mediconnect.medical.ordonnance;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_prescription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LignePrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordonnance_id", nullable = false)
    private Ordonnance ordonnance;

    @Column(nullable = false, length = 200)
    private String medicament;

    @Column(length = 100)
    private String dosage;

    @Column(length = 200)
    private String posologie;

    @Column(name = "duree_jours", length = 50)
    private String dureeJours;

    @Column(columnDefinition = "TEXT")
    private String instructions;
}