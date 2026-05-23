package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePatientResponse {

    private Long    patientId;
    private String  numPatient;
    private String  nomComplet;
    private String  email;
    private String  telephone;

    /** Mot de passe temporaire généré — à communiquer au patient */
    private String  motDePasseTemporaire;

    private String  message;
}