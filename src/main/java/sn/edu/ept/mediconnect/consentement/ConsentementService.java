package sn.edu.ept.mediconnect.consentement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.dtos.ConsentementResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentementService {

    private final ConsentementRepository          consentementRepository;
    private final SignatureConsentementRepository signatureRepository;
    private final PatientRepository               patientRepository;


    @Transactional(readOnly = true)
    public List<ConsentementResponse> getConsentements(Long patientId) {

        patientRepository.findById(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + patientId + ")"));

        // Tous les templates (un seul par TypeConsentement)
        List<Consentement> templates = consentementRepository.findAll();

        // Signatures existantes du patient, indexées par type
        Map<TypeConsentement, SignatureConsentement> signaturesParType =
                signatureRepository.findByPatientId(patientId)
                        .stream()
                        .collect(Collectors.toMap(
                                s -> s.getConsentement().getTypeConsentement(),
                                Function.identity()
                        ));

        List<ConsentementResponse> resultats = new ArrayList<>();
        for (Consentement template : templates) {
            SignatureConsentement sig = signaturesParType.get(template.getTypeConsentement());
            resultats.add(toResponse(template, sig));
        }
        return resultats;
    }

    @Transactional
    public void saveConsentement(Patient patient,
                                                   boolean  accepte,
                                                   User     signaturePar) {
        if (!accepte) {
            throw BusinessException.badRequest(
                    "L'acceptation de la politique de confidentialité est "
                  + "obligatoire pour créer un compte.");
        }

        Consentement template = consentementRepository
                .findByTypeConsentement(TypeConsentement.POLITIQUE_CONFIDENTIALITE)
                .orElseThrow(() -> new IllegalStateException(
                        "Template POLITIQUE_CONFIDENTIALITE introuvable. "
                      + "Vérifiez le DataInitializer."));

        SignatureConsentement signature = SignatureConsentement.builder()
                .patient(patient)
                .consentement(template)
                .accepte(true)
                .dateSignatureConsentement(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .modifiedBy(signaturePar)
                .build();

        signatureRepository.save(signature);
        log.info("Consentement POLITIQUE_CONFIDENTIALITE enregistré — patientId={}, par userId={}",
                patient.getId(), signaturePar.getId());
    }

    @Transactional
    public ConsentementResponse updateConsentement(Long             patientId,
                                                        TypeConsentement  type,
                                                        boolean           accepte,
                                                        User              modifiedBy) {
        if (type == TypeConsentement.POLITIQUE_CONFIDENTIALITE) {
            throw BusinessException.badRequest(
                    "La politique de confidentialité ne peut pas être modifiée "
                  + "après l'inscription.");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + patientId + ")"));

        Consentement template = consentementRepository
                .findByTypeConsentement(type)
                .orElseThrow(() -> BusinessException.notFound(
                        "Template introuvable pour le type : " + type));

        Optional<SignatureConsentement> existing =
                signatureRepository.findByPatientIdAndType(patientId, type);

        SignatureConsentement signature;

        if (existing.isPresent()) {
            // Mise à jour de la décision existante
            signature = existing.get();
            signature.setAccepte(accepte);
            signature.setModifiedBy(modifiedBy);
            signature.setDateModification(LocalDateTime.now());
        } else {
            // Première prise de position sur ce consentement
            signature = SignatureConsentement.builder()
                    .patient(patient)
                    .consentement(template)
                    .accepte(accepte)
                    .dateSignatureConsentement(LocalDateTime.now())
                    .dateModification(LocalDateTime.now())
                    .modifiedBy(modifiedBy)
                    .build();
        }

        signatureRepository.save(signature);
        log.info("Consentement {} {} — patientId={}, par userId={} (role={})",
                type, accepte ? "ACCEPTÉ" : "REFUSÉ",
                patientId, modifiedBy.getId(), modifiedBy.getRole());

        return toResponse(template, signature);
    }


    private ConsentementResponse toResponse(Consentement template, SignatureConsentement sig) {
        ConsentementResponse.ConsentementResponseBuilder b = ConsentementResponse.builder()
                .consentementId(template.getId())
                .type(template.getTypeConsentement())
                .titre(template.getTitre())
                .contenu(template.getContenu())
                .version(template.getVersion())
                .obligatoire(template.getObligatoire());

        if (sig != null) {
            b.signatureId(sig.getId())
             .accepte(sig.getAccepte())
             .dateSignature(sig.getDateSignatureConsentement())
             .dateModification(sig.getDateModification());

            if (sig.getModifiedBy() != null) {
                User u = sig.getModifiedBy();
                b.modifiedById(u.getId())
                 .modifiedByName(u.getPrenom() + " " + u.getNom())
                 .modifiedByRole(u.getRole().name());
            }
        }
        return b.build();
    }
}