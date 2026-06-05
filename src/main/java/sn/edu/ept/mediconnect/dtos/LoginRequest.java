package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;
import sn.edu.ept.mediconnect.validators.EmailOrPhoneRequired;

@Data
@EmailOrPhoneRequired
public class LoginRequest implements AuthenticationRequest {
    @Email(message = "Email invalide")
    private String email;

    private String phoneNumber;

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = PhoneNumberUtils.normalizePhoneNumber(phoneNumber);
        } else {
            this.phoneNumber = null;
        }
    }

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}