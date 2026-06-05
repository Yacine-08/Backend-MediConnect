package sn.edu.ept.mediconnect.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedecinRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank @Email
    private String email;

    private String telephone;

    @NotBlank(message = "Le numéro d'ordre MSAS est obligatoire")
    private String numeroOrdre;

    @NotBlank(message = "La spécialité est obligatoire")
    private String specialite;

    @NotBlank(message = "L'établissement est obligatoire")
    private String etablissement;

    private AdresseRequest adresse;
}