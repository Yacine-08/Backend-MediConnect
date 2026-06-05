package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sn.edu.ept.mediconnect.users.patient.GroupeSanguin;
import sn.edu.ept.mediconnect.users.patient.Sexe;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

import java.time.LocalDate;

@Data
public class UpdatePatientRequest {

    @Size(min = 2, max = 100)
    private String nom;

    @Size(min = 2, max = 100)
    private String prenom;

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
    private Boolean assurance;

    @Valid
    private AdresseRequest adresse;
}