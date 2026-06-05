package sn.edu.ept.mediconnect.users.medecin;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cardiologues")
@DiscriminatorValue("CARDIOLOGUE")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Cardiologue extends Medecin {
}