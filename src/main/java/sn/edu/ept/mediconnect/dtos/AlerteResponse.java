package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.alerte.NiveauAlerte;

import java.time.LocalDateTime;

@Data
@Builder
public class AlerteResponse {

    private Long id;
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private Long consultationId;
    private NiveauAlerte niveau;
    private String message;
    private String source;
    private Boolean acquittee;
    private LocalDateTime dateEmission;
    private LocalDateTime dateAcquittement;
}