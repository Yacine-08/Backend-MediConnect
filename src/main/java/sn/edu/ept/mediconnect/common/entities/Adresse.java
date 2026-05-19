package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "adresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String departement;

    @Column(length = 100)
    private String commune;
}