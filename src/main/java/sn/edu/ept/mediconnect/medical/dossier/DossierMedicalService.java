package sn.edu.ept.mediconnect.medical.dossier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.DossierMedicalRequest;
import sn.edu.ept.mediconnect.dtos.DossierMedicalResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierMedicalService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final PatientRepository patientRepository;

    // Consulter le dossier d'un patient
    @Transactional(readOnly = true)
    public DossierMedicalResponse getByPatientId(Long patientId) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Dossier médical introuvable pour le patient id=" + patientId));
        return toResponse(dossier);
    }

    // Consulter le dossier par son id
    @Transactional(readOnly = true)
    public DossierMedicalResponse getById(Long id) {
        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Dossier médical introuvable (id=" + id + ")"));
        return toResponse(dossier);
    }

    // Mettre à jour le dossier
    @Transactional
    public DossierMedicalResponse update(Long patientId, DossierMedicalRequest req) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Dossier médical introuvable pour le patient id=" + patientId));

        if (req.getAntecedentsMedicaux() != null)
            dossier.setAntecedentsMedicaux(req.getAntecedentsMedicaux());
        if (req.getAntecedentsChirurgicaux() != null)
            dossier.setAntecedentsChirurgicaux(req.getAntecedentsChirurgicaux());
        if (req.getAllergies() != null)
            dossier.setAllergies(req.getAllergies());
        if (req.getAntecedentsFamiliaux() != null)
            dossier.setAntecedentsFamiliaux(req.getAntecedentsFamiliaux());
        if (req.getTraitementEnCours() != null)
            dossier.setTraitementEnCours(req.getTraitementEnCours());

        dossierMedicalRepository.save(dossier);
        log.info("Dossier médical mis à jour pour le patient id={}", patientId);
        return toResponse(dossier);
    }

    // Archiver le dossier
    @Transactional
    public DossierMedicalResponse archiver(Long patientId) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Dossier médical introuvable pour le patient id=" + patientId));
        dossier.setStatut(StatutDossier.ARCHIVE);
        dossierMedicalRepository.save(dossier);
        log.info("Dossier médical archivé pour le patient id={}", patientId);
        return toResponse(dossier);
    }

    // Mapping entité → DTO
    public DossierMedicalResponse toResponse(DossierMedical d) {
        DossierMedicalResponse.DossierMedicalResponseBuilder b = DossierMedicalResponse.builder()
                .id(d.getId())
                .antecedentsMedicaux(d.getAntecedentsMedicaux())
                .antecedentsChirurgicaux(d.getAntecedentsChirurgicaux())
                .allergies(d.getAllergies())
                .antecedentsFamiliaux(d.getAntecedentsFamiliaux())
                .traitementEnCours(d.getTraitementEnCours())
                .statut(d.getStatut())
                .dateOuverture(d.getDateOuverture())
                .dateMiseAJour(d.getDateMiseAJour());

        if (d.getPatient() != null) {
            b.patientId(d.getPatient().getId())
                    .nomPatient(d.getPatient().getNom())
                    .prenomPatient(d.getPatient().getPrenom())
                    .numPatient(d.getPatient().getNumPatient());
        }

        return b.build();
    }
}