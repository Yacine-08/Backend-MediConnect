package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "adresses",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"region", "departement", "commune"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String region;

    @Column(length = 100, nullable = false)
    private String departement;

    @Column(length = 100, nullable = false)
    private String commune;
}