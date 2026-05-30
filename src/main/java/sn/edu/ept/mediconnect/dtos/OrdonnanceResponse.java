package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdonnanceResponse {

    private Long id;
    private Long consultationId;
    private String nomPatient;
    private String prenomPatient;
    private String nomMedecin;
    private String prenomMedecin;
    private Boolean signatureNumerique;
    private String qrCode;
    private LocalDateTime dateEmission;
    private LocalDateTime dateExpiration;
    private List<LignePrescriptionResponse> lignes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}