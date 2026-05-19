package sn.edu.ept.mediconnect.users.medecin;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cardiologues")
@Getter
@Setter
@NoArgsConstructor
public class Cardiologue extends Medecin {
}