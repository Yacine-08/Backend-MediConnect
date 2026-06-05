package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import sn.edu.ept.mediconnect.common.entities.Role;
import sn.edu.ept.mediconnect.users.patient.GroupeSanguin;
import sn.edu.ept.mediconnect.users.patient.Sexe;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;
import sn.edu.ept.mediconnect.dtos.AdresseRequest;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String prenom;

    // Au moins un des deux requis (validé dans le service)
    @Email(message = "Format email invalide")
    private String email;

    private String telephone;
    public void setTelephone(String telephone) {
        if (telephone != null && !telephone.trim().isEmpty()) {
            this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        } else {
            this.telephone = null;
        }
    }

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Minimum 8 caractères")
    private String motDePasse;

    // champs MEDECIN / CARDIOLOGUE

    private String numOrdre;

    private String section;       // A ou B
    private String specialite;

    // UUID de l'hôpital d'affectation
    private String etablissement;

    // champs PATIENT
    private LocalDate dateNaissance;
    private Sexe sexe;
    private GroupeSanguin groupeSanguin;
    private AdresseRequest adresse;
    private Boolean   assurance;

    // champs INFIRMIER
    private String serviceAffecte;
    private String hopital;

}