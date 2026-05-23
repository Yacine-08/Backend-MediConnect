package sn.edu.ept.mediconnect.dtos;

import lombok.Data;

@Data
public class DossierMedicalRequest {

    private String antecedentsMedicaux;
    private String antecedentsChirurgicaux;
    private String allergies;
    private String antecedentsFamiliaux;
    private String traitementEnCours;
}