package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.users.medecin.Section;

@Entity
@Table(name = "ordre_medecins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreMedecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numéro d'ordre unique (ex: "1056/P", "539", "2997")
    @Column(name = "num_ordre", unique = true, nullable = false)
    private String numOrdre;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(length = 200)
    private String specialite;

    // Section A ou B
    @Column(nullable = false, length = 5)
    private String section;

    @Column(nullable = false)
    private Boolean actif = true;
}