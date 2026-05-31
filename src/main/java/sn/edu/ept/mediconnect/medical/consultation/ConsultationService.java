package sn.edu.ept.mediconnect.medical.consultation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.ConsultationMedecineRequest;
import sn.edu.ept.mediconnect.dtos.ConsultationRequest;
import sn.edu.ept.mediconnect.dtos.ConsultationResponse;
import sn.edu.ept.mediconnect.dtos.ConstantesRequest;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.rendezvous.RendezVous;
import sn.edu.ept.mediconnect.medical.rendezvous.RendezVousRepository;
import sn.edu.ept.mediconnect.medical.rendezvous.StatutRendezVous;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
import sn.edu.ept.mediconnect.users.infirmier.InfirmierRepository;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.medecin.MedecinRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;
import sn.edu.ept.mediconnect.medical.alerte.AlerteRepository;
import sn.edu.ept.mediconnect.medical.alerte.Alerte;
import sn.edu.ept.mediconnect.medical.alerte.NiveauAlerte;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final InfirmierRepository infirmierRepository;
    private final RendezVousRepository rendezVousRepository;
    private final AlerteRepository alerteRepository;

    // Créer une consultation — Médecin / Cardiologue
    @Transactional
    public ConsultationResponse create(ConsultationRequest req) {

        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + req.getPatientId() + ")"));

        Medecin medecin = medecinRepository.findById(req.getMedecinId())
                .orElseThrow(() -> BusinessException.notFound(
                        "Médecin introuvable (id=" + req.getMedecinId() + ")"));

        Consultation consultation = Consultation.builder()
                .patient(patient)
                .medecin(medecin)
                .statut(StatutConsultation.EN_ATTENTE)
                .build();

        // Lier au rendez-vous si fourni
        if (req.getRendezVousId() != null) {
            RendezVous rdv = rendezVousRepository.findById(req.getRendezVousId())
                    .orElseThrow(() -> BusinessException.notFound(
                            "Rendez-vous introuvable (id=" + req.getRendezVousId() + ")"));

            // Vérifier que le RDV est confirmé
            if (rdv.getStatut() != StatutRendezVous.CONFIRME) {
                throw BusinessException.badRequest(
                        "Le rendez-vous doit être confirmé pour démarrer une consultation.");
            }

            consultation.setRendezVous(rdv);
        }

        consultationRepository.save(consultation);
        log.info("Consultation créée : patient={} medecin={}", req.getPatientId(), req.getMedecinId());
        return toResponse(consultation);
    }

    // Phase 1 — Infirmier prend les constantes
    @Transactional
    public ConsultationResponse prendreConstantes(Long consultationId, Long infirmierId, ConstantesRequest req) {

        Consultation consultation = find(consultationId);

        if (consultation.getStatut() != StatutConsultation.EN_ATTENTE) {
            throw BusinessException.badRequest(
                    "Les constantes ne peuvent être prises que pour une consultation en attente.");
        }

        Infirmier infirmier = infirmierRepository.findById(infirmierId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Infirmier introuvable (id=" + infirmierId + ")"));

        consultation.setInfirmier(infirmier);
        consultation.setTensionArterielle(req.getTensionArterielle());
        consultation.setFrequenceCardiaque(req.getFrequenceCardiaque());
        consultation.setTemperature(req.getTemperature());
        consultation.setPoids(req.getPoids());
        consultation.setTaille(req.getTaille());
        consultation.setSpo2(req.getSpo2());
        consultation.setStatut(StatutConsultation.CONSTANTES_PRISES);

        consultationRepository.save(consultation);
        verifierConstantesEtCreerAlertes(consultation, req);
        log.info("Constantes prises pour la consultation id={} par infirmier id={}", consultationId, infirmierId);
        return toResponse(consultation);
    }

    private void verifierConstantesEtCreerAlertes(Consultation consultation, ConstantesRequest req) {

        if (req.getSpo2() != null && req.getSpo2() < 90) {
            alerteRepository.save(Alerte.builder()
                    .patient(consultation.getPatient())
                    .consultation(consultation)
                    .niveau(NiveauAlerte.CRITIQUE)
                    .message("SpO2 critique : " + req.getSpo2() + "%")
                    .source("Prise des constantes")
                    .acquittee(false)
                    .build());
        }

        if (req.getFrequenceCardiaque() != null &&
                (req.getFrequenceCardiaque() < 40 || req.getFrequenceCardiaque() > 150)) {
            alerteRepository.save(Alerte.builder()
                    .patient(consultation.getPatient())
                    .consultation(consultation)
                    .niveau(NiveauAlerte.CRITIQUE)
                    .message("Fréquence cardiaque anormale : " + req.getFrequenceCardiaque() + " bpm")
                    .source("Prise des constantes")
                    .acquittee(false)
                    .build());
        }

        if (req.getTemperature() != null && req.getTemperature() > 40) {
            alerteRepository.save(Alerte.builder()
                    .patient(consultation.getPatient())
                    .consultation(consultation)
                    .niveau(NiveauAlerte.URGENT)
                    .message("Température élevée : " + req.getTemperature() + "°C")
                    .source("Prise des constantes")
                    .acquittee(false)
                    .build());
        }

        if (req.getTensionArterielle() != null) {
            String[] valeurs = req.getTensionArterielle().split("/");
            if (valeurs.length == 2) {
                int systolique = Integer.parseInt(valeurs[0]);
                int diastolique = Integer.parseInt(valeurs[1]);
                if (systolique > 180 || diastolique > 110) {
                    alerteRepository.save(Alerte.builder()
                            .patient(consultation.getPatient())
                            .consultation(consultation)
                            .niveau(NiveauAlerte.CRITIQUE)
                            .message("Hypertension sévère : " + req.getTensionArterielle())
                            .source("Prise des constantes")
                            .acquittee(false)
                            .build());
                }
            }
        }
    }

    // Phase 2 — Médecin complète la consultation
    @Transactional
    public ConsultationResponse completerConsultation(Long consultationId, ConsultationMedecineRequest req) {

        Consultation consultation = find(consultationId);

        if (consultation.getStatut() != StatutConsultation.CONSTANTES_PRISES) {
            throw BusinessException.badRequest(
                    "Les constantes doivent être prises avant de compléter la consultation.");
        }

        consultation.setMotif(req.getMotif());
        consultation.setAnamnese(req.getAnamnese());
        consultation.setExamenClinique(req.getExamenClinique());
        consultation.setDiagnostic(req.getDiagnostic());
        consultation.setAnalyseIa(req.getAnalyseIa());
        consultation.setScoreConfiance(req.getScoreConfiance());
        consultation.setPathologiesDetectees(req.getPathologiesDetectees());
        consultation.setCompteRendu(req.getCompteRendu());
        consultation.setStatut(StatutConsultation.EN_COURS);

        consultationRepository.save(consultation);
        log.info("Consultation complétée par le médecin : id={}", consultationId);
        return toResponse(consultation);
    }

    // Terminer une consultation
    @Transactional
    public ConsultationResponse terminer(Long consultationId) {
        Consultation consultation = find(consultationId);

        if (consultation.getStatut() != StatutConsultation.EN_COURS) {
            throw BusinessException.badRequest(
                    "Seule une consultation en cours peut être terminée.");
        }

        consultation.setStatut(StatutConsultation.TERMINEE);
        consultationRepository.save(consultation);
        log.info("Consultation terminée : id={}", consultationId);
        return toResponse(consultation);
    }

    // Annuler une consultation
    @Transactional
    public ConsultationResponse annuler(Long consultationId) {
        Consultation consultation = find(consultationId);

        if (consultation.getStatut() == StatutConsultation.TERMINEE) {
            throw BusinessException.badRequest(
                    "Une consultation terminée ne peut pas être annulée.");
        }

        consultation.setStatut(StatutConsultation.ANNULEE);
        consultationRepository.save(consultation);
        log.info("Consultation annulée : id={}", consultationId);
        return toResponse(consultation);
    }

    // Lister les consultations d'un patient
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getByPatient(Long patientId) {
        return consultationRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister les consultations d'un médecin
    @Transactional(readOnly = true)
    public List<ConsultationResponse> getByMedecin(Long medecinId) {
        return consultationRepository.findByMedecinId(medecinId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'une consultation
    @Transactional(readOnly = true)
    public ConsultationResponse getById(Long id) {
        return toResponse(find(id));
    }

    private Consultation find(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Consultation introuvable (id=" + id + ")"));
    }

    public ConsultationResponse toResponse(Consultation c) {
        ConsultationResponse.ConsultationResponseBuilder b = ConsultationResponse.builder()
                .id(c.getId())
                .dateHeure(c.getDateHeure())
                .statut(c.getStatut())
                .tensionArterielle(c.getTensionArterielle())
                .frequenceCardiaque(c.getFrequenceCardiaque())
                .temperature(c.getTemperature())
                .poids(c.getPoids())
                .taille(c.getTaille())
                .spo2(c.getSpo2())
                .motif(c.getMotif())
                .anamnese(c.getAnamnese())
                .examenClinique(c.getExamenClinique())
                .diagnostic(c.getDiagnostic())
                .analyseIa(c.getAnalyseIa())
                .scoreConfiance(c.getScoreConfiance())
                .pathologiesDetectees(c.getPathologiesDetectees())
                .compteRendu(c.getCompteRendu())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt());

        if (c.getPatient() != null) {
            b.patientId(c.getPatient().getId())
                    .nomPatient(c.getPatient().getNom())
                    .prenomPatient(c.getPatient().getPrenom());
        }

        if (c.getMedecin() != null) {
            b.medecinId(c.getMedecin().getId())
                    .nomMedecin(c.getMedecin().getNom())
                    .prenomMedecin(c.getMedecin().getPrenom());
        }

        if (c.getInfirmier() != null) {
            b.infirmierId(c.getInfirmier().getId())
                    .nomInfirmier(c.getInfirmier().getNom())
                    .prenomInfirmier(c.getInfirmier().getPrenom());
        }

        if (c.getRendezVous() != null) {
            b.rendezVousId(c.getRendezVous().getId());
        }

        return b.build();
    }
}
