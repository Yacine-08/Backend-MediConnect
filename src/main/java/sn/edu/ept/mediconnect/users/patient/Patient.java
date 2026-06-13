package sn.edu.ept.mediconnect.users.patient;

import jakarta.persistence.*;
import lombok.*;
// import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@DiscriminatorValue("PATIENT")
@PrimaryKeyJoinColumn(name = "id")
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


    @Column(nullable = false)
    private Boolean assurance = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par")
    private User creePar;

    @Column(name = "demande_suppression", columnDefinition = "boolean not null default false")
    private Boolean demandeSuppressionEnAttente = false;

}
