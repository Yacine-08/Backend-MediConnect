package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.examen.ExamenType;
import sn.edu.ept.mediconnect.medical.examen.StatutExamen;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamenResponse {

    private Long id;
    private Long consultationId;
    private String nomPatient;
    private String prenomPatient;
    private String nomMedecin;
    private String prenomMedecin;
    private String nom;
    private ExamenType type;
    private String fichierUrl;
    private String format;
    private Long tailleFichier;
    private StatutExamen statut;
    private LocalDateTime dateAcquisition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}