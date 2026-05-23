package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.dossier.StatutDossier;

import java.time.LocalDateTime;

@Data
@Builder
public class DossierMedicalResponse {

    private Long id;
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private String numPatient;
    private String antecedentsMedicaux;
    private String antecedentsChirurgicaux;
    private String allergies;
    private String antecedentsFamiliaux;
    private String traitementEnCours;
    private StatutDossier statut;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateMiseAJour;
}