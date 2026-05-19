package sn.edu.ept.mediconnect.common.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.edu.ept.mediconnect.users.User;

@Entity
@Table(name = "hopitaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hopital extends User {
    @Column(nullable = false, length = 200)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;

    @Column(name = "type_etablissement", length = 100)
    private TypeEtablissement typeEtablissement;

    @Column(length = 20)
    private String telephone;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "longitude")
    private Float longitude;

}
