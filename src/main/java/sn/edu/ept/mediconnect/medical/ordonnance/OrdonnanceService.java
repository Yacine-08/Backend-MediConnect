package sn.edu.ept.mediconnect.medical.ordonnance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.LignePrescriptionRequest;
import sn.edu.ept.mediconnect.dtos.LignePrescriptionResponse;
import sn.edu.ept.mediconnect.dtos.OrdonnanceRequest;
import sn.edu.ept.mediconnect.dtos.OrdonnanceResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.consultation.Consultation;
import sn.edu.ept.mediconnect.medical.consultation.ConsultationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdonnanceService {

    private final OrdonnanceRepository ordonnanceRepository;
    private final LignePrescriptionRepository lignePrescriptionRepository;
    private final ConsultationRepository consultationRepository;

    // Créer une ordonnance
    @Transactional
    public OrdonnanceResponse create(OrdonnanceRequest req) {

        Consultation consultation = consultationRepository.findById(req.getConsultationId())
                .orElseThrow(() -> BusinessException.notFound(
                        "Consultation introuvable (id=" + req.getConsultationId() + ")"));

        // Vérifier qu'il n'y a pas déjà une ordonnance pour cette consultation
        if (ordonnanceRepository.existsByConsultationId(req.getConsultationId())) {
            throw BusinessException.conflict(
                    "Une ordonnance existe déjà pour cette consultation.");
        }

        Ordonnance ordonnance = Ordonnance.builder()
                .consultation(consultation)
                .dateExpiration(req.getDateExpiration())
                .signatureNumerique(false)
                .build();

        ordonnanceRepository.save(ordonnance);

        // Créer les lignes de prescription
        List<LignePrescription> lignes = req.getLignes().stream()
                .map(l -> LignePrescription.builder()
                        .ordonnance(ordonnance)
                        .medicament(l.getMedicament())
                        .dosage(l.getDosage())
                        .posologie(l.getPosologie())
                        .dureeJours(l.getDureeJours())
                        .instructions(l.getInstructions())
                        .build())
                .collect(Collectors.toList());

        lignePrescriptionRepository.saveAll(lignes);
        ordonnance.setLignes(lignes);

        log.info("Ordonnance créée : consultation={}", req.getConsultationId());
        return toResponse(ordonnance);
    }

    // Signer une ordonnance
    @Transactional
    public OrdonnanceResponse signer(Long id) {
        Ordonnance ordonnance = find(id);

        if (Boolean.TRUE.equals(ordonnance.getSignatureNumerique())) {
            throw BusinessException.badRequest("Cette ordonnance est déjà signée.");
        }

        ordonnance.setSignatureNumerique(true);
        ordonnance.setQrCode(UUID.randomUUID().toString());

        ordonnanceRepository.save(ordonnance);
        log.info("Ordonnance signée : id={}", id);
        return toResponse(ordonnance);
    }

    // Consulter l'ordonnance d'une consultation
    @Transactional(readOnly = true)
    public OrdonnanceResponse getByConsultation(Long consultationId) {
        Ordonnance ordonnance = ordonnanceRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Aucune ordonnance pour cette consultation."));
        return toResponse(ordonnance);
    }

    // Lister les ordonnances d'un patient
    @Transactional(readOnly = true)
    public List<OrdonnanceResponse> getByPatient(Long patientId) {
        return ordonnanceRepository.findByConsultationPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister les ordonnances d'un médecin
    @Transactional(readOnly = true)
    public List<OrdonnanceResponse> getByMedecin(Long medecinId) {
        return ordonnanceRepository.findByConsultationMedecinId(medecinId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'une ordonnance
    @Transactional(readOnly = true)
    public OrdonnanceResponse getById(Long id) {
        return toResponse(find(id));
    }

    private Ordonnance find(Long id) {
        return ordonnanceRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Ordonnance introuvable (id=" + id + ")"));
    }

    public OrdonnanceResponse toResponse(Ordonnance o) {
        OrdonnanceResponse.OrdonnanceResponseBuilder b = OrdonnanceResponse.builder()
                .id(o.getId())
                .consultationId(o.getConsultation().getId())
                .signatureNumerique(o.getSignatureNumerique())
                .qrCode(o.getQrCode())
                .dateEmission(o.getDateEmission())
                .dateExpiration(o.getDateExpiration())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt());

        if (o.getConsultation().getPatient() != null) {
            b.nomPatient(o.getConsultation().getPatient().getNom())
                    .prenomPatient(o.getConsultation().getPatient().getPrenom());
        }

        if (o.getConsultation().getMedecin() != null) {
            b.nomMedecin(o.getConsultation().getMedecin().getNom())
                    .prenomMedecin(o.getConsultation().getMedecin().getPrenom());
        }

        if (o.getLignes() != null) {
            b.lignes(o.getLignes().stream()
                    .map(l -> LignePrescriptionResponse.builder()
                            .id(l.getId())
                            .medicament(l.getMedicament())
                            .dosage(l.getDosage())
                            .posologie(l.getPosologie())
                            .dureeJours(l.getDureeJours())
                            .instructions(l.getInstructions())
                            .build())
                    .collect(Collectors.toList()));
        }

        return b.build();
    }
}