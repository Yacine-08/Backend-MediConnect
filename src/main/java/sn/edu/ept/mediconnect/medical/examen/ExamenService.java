package sn.edu.ept.mediconnect.medical.examen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.ExamenRequest;
import sn.edu.ept.mediconnect.dtos.ExamenResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import sn.edu.ept.mediconnect.medical.consultation.ConsultationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final ConsultationRepository consultationRepository;

    // Prescrire un examen
    @Transactional
    public ExamenResponse create(Long consultationId, ExamenRequest req) {

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Consultation introuvable (id=" + consultationId + ")"));

        Examen examen = Examen.builder()
                .consultation(consultation)
                .nom(req.getNom())
                .type(req.getType())
                .fichierUrl(req.getFichierUrl())
                .format(req.getFormat())
                .tailleFichier(req.getTailleFichier())
                .statut(StatutExamen.EN_ATTENTE)
                .build();

        examenRepository.save(examen);
        log.info("Examen prescrit : consultation={} type={}", consultationId, req.getType());
        return toResponse(examen);
    }

    // Mettre à jour le résultat d'un examen — marquer comme réalisé
    @Transactional
    public ExamenResponse marquerRealise(Long id, String fichierUrl, String format, Long tailleFichier) {
        Examen examen = find(id);

        if (examen.getStatut() == StatutExamen.ANNULE) {
            throw BusinessException.badRequest("Un examen annulé ne peut pas être réalisé.");
        }

        examen.setFichierUrl(fichierUrl);
        examen.setFormat(format);
        examen.setTailleFichier(tailleFichier);
        examen.setStatut(StatutExamen.REALISE);
        examen.setDateAcquisition(LocalDateTime.now());

        examenRepository.save(examen);
        log.info("Examen réalisé : id={}", id);
        return toResponse(examen);
    }

    // Annuler un examen
    @Transactional
    public ExamenResponse annuler(Long id) {
        Examen examen = find(id);

        if (examen.getStatut() == StatutExamen.REALISE) {
            throw BusinessException.badRequest("Un examen déjà réalisé ne peut pas être annulé.");
        }

        examen.setStatut(StatutExamen.ANNULE);
        examenRepository.save(examen);
        log.info("Examen annulé : id={}", id);
        return toResponse(examen);
    }

    // Lister les examens d'une consultation
    @Transactional(readOnly = true)
    public List<ExamenResponse> getByConsultation(Long consultationId) {
        return examenRepository.findByConsultationId(consultationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister les examens d'un patient
    @Transactional(readOnly = true)
    public List<ExamenResponse> getByPatient(Long patientId) {
        return examenRepository.findByConsultationPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'un examen
    @Transactional(readOnly = true)
    public ExamenResponse getById(Long id) {
        return toResponse(find(id));
    }

    private Examen find(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Examen introuvable (id=" + id + ")"));
    }

    public ExamenResponse toResponse(Examen e) {
        ExamenResponse.ExamenResponseBuilder b = ExamenResponse.builder()
                .id(e.getId())
                .consultationId(e.getConsultation().getId())
                .nom(e.getNom())
                .type(e.getType())
                .fichierUrl(e.getFichierUrl())
                .format(e.getFormat())
                .tailleFichier(e.getTailleFichier())
                .statut(e.getStatut())
                .dateAcquisition(e.getDateAcquisition())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt());

        if (e.getConsultation().getPatient() != null) {
            b.nomPatient(e.getConsultation().getPatient().getNom())
                    .prenomPatient(e.getConsultation().getPatient().getPrenom());
        }

        if (e.getConsultation().getMedecin() != null) {
            b.nomMedecin(e.getConsultation().getMedecin().getNom())
                    .prenomMedecin(e.getConsultation().getMedecin().getPrenom());
        }

        return b.build();
    }
}
