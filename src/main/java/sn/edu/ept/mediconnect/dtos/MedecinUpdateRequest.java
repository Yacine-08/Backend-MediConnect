package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

@Data
public class MedecinUpdateRequest {

    @Size(min = 2, max = 100)
    private String nom;

    @Size(min = 2, max = 100)
    private String prenom;

    @Email(message = "Format email invalide")
    private String email;

    private String telephone;
    public void setTelephone(String telephone) {
        if (telephone != null && !telephone.trim().isEmpty())
            this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        else
            this.telephone = null;
    }

    private Boolean disponible;

    private String nomEtablissement;

    @Valid
    private AdresseRequest adresse;

}