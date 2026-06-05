package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InfirmierResponse {

    private Long   id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String serviceAffecte;
    private String hopital;
    private String region;
    private String departement;
    private String commune;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}