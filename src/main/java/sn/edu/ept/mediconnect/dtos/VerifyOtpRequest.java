package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

@Data
public class VerifyOtpRequest {

    @Email
    private String email;

    private String telephone;

    @NotBlank(message = "Le code OTP est obligatoire")
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir 6 chiffres")
    private String code;

    public void setTelephone(String telephone) {
        if (telephone != null && !telephone.trim().isEmpty()) {
            this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        } else {
            this.telephone = null;
        }
    }
}
