package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.transfert.StatutTransfert;
import sn.edu.ept.mediconnect.medical.transfert.TypeTransfert;

import java.time.LocalDateTime;

@Data
@Builder
public class TransfertResponse {

    private Long id;
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private Long medecinId;
    private String nomMedecin;
    private String prenomMedecin;
    private String hopitalSource;
    private String hopitalDestination;
    private TypeTransfert type;
    private String motif;
    private String compteRendu;
    private StatutTransfert statut;
    private LocalDateTime dateTransfert;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
