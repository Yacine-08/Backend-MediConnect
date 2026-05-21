package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RegisterResponse {

    private Long  userId;
    private String  message;
//    private String  email;
//    private String  nom;
//    private String  prenom;
//    private String  role;
//    private String  telephone;
    private String  token;
    private Date createdAt;
    private Boolean otpEnvoye;
}