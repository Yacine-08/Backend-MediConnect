package sn.edu.ept.mediconnect.users.medecin;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.users.User;

@Entity
@Table(name = "medecins")
@DiscriminatorValue("MEDECIN")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medecin extends User {
    @Column(name = "num_ordre", unique = true, length = 50)
    private String numOrdre;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Section section;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Specialite specialite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_id")
    private Hopital etablissement;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(name = "is_verify", nullable = false)
    private Boolean verified = false;


    public boolean verifierOrdre() {
        return numOrdre != null && !numOrdre.isBlank();
    }

}
