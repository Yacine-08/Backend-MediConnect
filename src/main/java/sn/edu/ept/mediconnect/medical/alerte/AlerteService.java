package sn.edu.ept.mediconnect.medical.alerte;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.AlerteRequest;
import sn.edu.ept.mediconnect.dtos.AlerteResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import sn.edu.ept.mediconnect.medical.consultation.ConsultationRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;

    // Créer une alerte
    @Transactional
    public AlerteResponse create(AlerteRequest req) {

        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + req.getPatientId() + ")"));

        Alerte alerte = Alerte.builder()
                .patient(patient)
                .niveau(req.getNiveau())
                .message(req.getMessage())
                .source(req.getSource())
                .acquittee(false)
                .build();

        if (req.getConsultationId() != null) {
            Consultation consultation = consultationRepository.findById(req.getConsultationId())
                    .orElseThrow(() -> BusinessException.notFound(
                            "Consultation introuvable (id=" + req.getConsultationId() + ")"));
            alerte.setConsultation(consultation);
        }

        alerteRepository.save(alerte);
        log.info("Alerte créée : patient={} niveau={}", req.getPatientId(), req.getNiveau());
        return toResponse(alerte);
    }

    // Acquitter une alerte
    @Transactional
    public AlerteResponse acquitter(Long id) {
        Alerte alerte = find(id);

        if (Boolean.TRUE.equals(alerte.getAcquittee())) {
            throw BusinessException.badRequest("Cette alerte est déjà acquittée.");
        }

        alerte.setAcquittee(true);
        alerte.setDateAcquittement(LocalDateTime.now());
        alerteRepository.save(alerte);
        log.info("Alerte acquittée : id={}", id);
        return toResponse(alerte);
    }

    // Lister toutes les alertes non acquittées
    @Transactional(readOnly = true)
    public List<AlerteResponse> getNonAcquittees() {
        return alerteRepository.findByAcquittee(false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister les alertes d'un patient
    @Transactional(readOnly = true)
    public List<AlerteResponse> getByPatient(Long patientId) {
        return alerteRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister les alertes d'une consultation
    @Transactional(readOnly = true)
    public List<AlerteResponse> getByConsultation(Long consultationId) {
        return alerteRepository.findByConsultationId(consultationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister par niveau
    @Transactional(readOnly = true)
    public List<AlerteResponse> getByNiveau(NiveauAlerte niveau) {
        return alerteRepository.findByNiveau(niveau)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'une alerte
    @Transactional(readOnly = true)
    public AlerteResponse getById(Long id) {
        return toResponse(find(id));
    }

    private Alerte find(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Alerte introuvable (id=" + id + ")"));
    }

    public AlerteResponse toResponse(Alerte a) {
        AlerteResponse.AlerteResponseBuilder b = AlerteResponse.builder()
                .id(a.getId())
                .niveau(a.getNiveau())
                .message(a.getMessage())
                .source(a.getSource())
                .acquittee(a.getAcquittee())
                .dateEmission(a.getDateEmission())
                .dateAcquittement(a.getDateAcquittement());

        if (a.getPatient() != null) {
            b.patientId(a.getPatient().getId())
                    .nomPatient(a.getPatient().getNom())
                    .prenomPatient(a.getPatient().getPrenom());
        }

        if (a.getConsultation() != null) {
            b.consultationId(a.getConsultation().getId());
        }

        return b.build();
    }
}