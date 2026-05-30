package sn.edu.ept.mediconnect.dtos;

import lombok.Data;

@Data
public class ConsultationMedecineRequest {

    private String motif;
    private String anamnese;
    private String examenClinique;
    private String diagnostic;
    private String analyseIa;
    private Float scoreConfiance;
    private String pathologiesDetectees;
    private String compteRendu;
}