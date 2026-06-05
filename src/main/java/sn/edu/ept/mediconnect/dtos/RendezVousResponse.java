package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.rendezvous.StatutRendezVous;
import sn.edu.ept.mediconnect.medical.rendezvous.TypeRendezVous;

import java.time.LocalDateTime;

@Data
@Builder
public class RendezVousResponse {

    private Long id;
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private Long medecinId;
    private String nomMedecin;
    private String prenomMedecin;
    private String hopital;
    private LocalDateTime dateHeure;
    private TypeRendezVous type;
    private StatutRendezVous statut;
    private String motif;
    private String lienVideo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}