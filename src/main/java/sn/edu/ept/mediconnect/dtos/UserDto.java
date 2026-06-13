package sn.edu.ept.mediconnect.dtos;

import lombok.Data;
import sn.edu.ept.mediconnect.common.entities.Role;

import java.util.List;

@Data
public class UserDto {
    private Long userId;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private Role role;
    private String etablissement;
}