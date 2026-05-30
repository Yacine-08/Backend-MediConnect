package sn.edu.ept.mediconnect.dtos;

import lombok.Builder;
import lombok.Data;
import sn.edu.ept.mediconnect.medical.consultation.StatutConsultation;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsultationResponse {

    private Long id;

    // Patient
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;

    // Médecin
    private Long medecinId;
    private String nomMedecin;
    private String prenomMedecin;

    // Infirmier
    private Long infirmierId;
    private String nomInfirmier;
    private String prenomInfirmier;

    // Rendez-vous
    private Long rendezVousId;

    private LocalDateTime dateHeure;
    private StatutConsultation statut;

    // Phase 1 — Constantes
    private String tensionArterielle;
    private Integer frequenceCardiaque;
    private Float temperature;
    private Float poids;
    private Float taille;
    private Float spo2;

    // Phase 2 — Clinique
    private String motif;
    private String anamnese;
    private String examenClinique;
    private String diagnostic;
    private String analyseIa;
    private Float scoreConfiance;
    private String pathologiesDetectees;
    private String compteRendu;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}