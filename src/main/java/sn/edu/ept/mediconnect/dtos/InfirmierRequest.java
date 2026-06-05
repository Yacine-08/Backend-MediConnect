package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;


@Data
public class InfirmierRequest {

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

    @Size(max = 200)
    private String serviceAffecte;

    private String nomHopital;

    @Valid
    private AdresseRequest adresse;

}