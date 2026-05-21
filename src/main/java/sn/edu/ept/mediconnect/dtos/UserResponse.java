package sn.edu.ept.mediconnect.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import sn.edu.ept.mediconnect.common.entities.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private LocalDateTime createdAt;
    private Role role;

}
