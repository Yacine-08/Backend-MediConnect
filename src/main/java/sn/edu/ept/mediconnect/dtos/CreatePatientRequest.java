package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import sn.edu.ept.mediconnect.users.patient.GroupeSanguin;
import sn.edu.ept.mediconnect.users.patient.Sexe;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

import java.time.LocalDate;

@Data
public class CreatePatientRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    private String prenom;

    // Au moins email ou telephone requis (validé dans le service)
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

    private LocalDate dateNaissance;

    private Sexe sexe;

    private GroupeSanguin groupeSanguin;

    private Boolean assurance = false;

    @Valid
    private AdresseRequest adresse;

    // consentement à la politique de confidentialité (obligatoire)
    // L'infirmier coche cette case après lecture des conditions au patient
    @NotNull(message = "L'acceptation de la politique de confidentialité est obligatoire")
    @AssertTrue(message = "La politique de confidentialité doit être acceptée pour créer le compte")
    private Boolean acceptePolitiqueConfidentialite;
}