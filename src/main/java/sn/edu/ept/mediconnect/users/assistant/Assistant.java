package sn.edu.ept.mediconnect.users.assistant;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.users.User;

@Entity
@Table(name = "assistants")
@DiscriminatorValue("ASSISTANT")
@PrimaryKeyJoinColumn(name = "id")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assistant extends User {

    @Column(name = "service_affecte", length = 200)
    private String serviceAffecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hopital_id")
    private Hopital hopital;
}
