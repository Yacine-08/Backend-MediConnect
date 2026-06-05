package sn.edu.ept.mediconnect.users.infirmier;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.users.User;

@Entity
@Table(name = "infirmiers")
@DiscriminatorValue("INFIRMIER")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Infirmier extends User{

    @Column(name = "service_affecte", length = 200)
    private String serviceAffecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hopital_id")
    private Hopital hopital;

}
