package sn.edu.ept.mediconnect.dtos;


import lombok.Data;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

@Data
public class ResendOtpRequest {
    private String email;
    private String telephone;
    
    public void setPhoneNumber(String telephone) {
        if (telephone != null && !telephone.trim().isEmpty()) {
            this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        } else {
            this.telephone = null;
        }
    }
}
