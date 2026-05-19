package sn.edu.ept.mediconnect.users.patient;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends User {

    @Column(name = "numero_patient", nullable = false, unique = true)
    private String numPatient;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Sexe sexe;

    @Enumerated(EnumType.STRING)
    @Column(name = "groupe_sanguin", length = 10)
    private GroupeSanguin groupeSanguin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;

    @Column(nullable = false)
    private Boolean assurance = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par")
    private Infirmier creePar;

}
