package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "commune")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;
}