package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ordre_medecins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @Column(name = "num_ordre", unique = true, nullable = false)
    private String numOrdre;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private String specialite;

    private String section;

    @Column(nullable = false)
    private Boolean actif = true;
}