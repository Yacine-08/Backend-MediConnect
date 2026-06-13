package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.users.patient.GroupeSanguin;
import sn.edu.ept.mediconnect.users.patient.Sexe;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientResponse {

    private Long   id;
    private String numPatient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private Sexe    sexe;
    private GroupeSanguin groupeSanguin;
    private Boolean assurance;
    private Boolean actif;

    // Utilisateur (assistant) qui a créé le compte
    private Long   creeParId;
    private String creeParNomComplet;

    // Adresse
    private String region;
    private String departement;
    private String commune;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
