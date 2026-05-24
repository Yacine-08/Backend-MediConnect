package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedecinResponse {

    private Long    id;
    private String  nom;
    private String  prenom;
    private String  email;
    private String  telephone;
    private String  numOrdre;
    private String  section;
    private String  specialite;
    private String  etablissement;
    private Boolean disponible;
    private Boolean verified;
    private Boolean actif;

    // Adresse
    private AdresseRequest adresse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}