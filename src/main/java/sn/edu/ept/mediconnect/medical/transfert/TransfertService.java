package sn.edu.ept.mediconnect.medical.transfert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.dtos.TransfertRequest;
import sn.edu.ept.mediconnect.dtos.TransfertResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.medecin.MedecinRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransfertService {

    private final TransfertRepository transfertRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final HopitalRepository hopitalRepository;

    // Créer un transfert
    @Transactional
    public TransfertResponse create(String numPatient, Long medecinId, TransfertRequest req) {

        Patient patient = patientRepository.findByNumPatient(numPatient)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable : " + numPatient));

        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Médecin introuvable (id=" + medecinId + ")"));

        Hopital hopitalSource = hopitalRepository.findByNom(req.getNomHopitalSource())
                .orElseThrow(() -> BusinessException.notFound(
                        "Hôpital source introuvable : " + req.getNomHopitalSource()));

        Hopital hopitalDestination = hopitalRepository.findByNom(req.getNomHopitalDestination())
                .orElseThrow(() -> BusinessException.notFound(
                        "Hôpital destination introuvable : " + req.getNomHopitalDestination()));

        if (hopitalSource.getId().equals(hopitalDestination.getId())) {
            throw BusinessException.badRequest(
                    "L'hôpital source et destination ne peuvent pas être identiques.");
        }

        Transfert transfert = Transfert.builder()
                .patient(patient)
                .medecin(medecin)
                .hopitalSource(hopitalSource)
                .hopitalDestination(hopitalDestination)
                .type(req.getType())
                .motif(req.getMotif())
                .compteRendu(req.getCompteRendu())
                .statut(StatutTransfert.EN_ATTENTE)
                .build();

        transfertRepository.save(transfert);
        log.info("Transfert créé : patient={} source={} destination={}",
                numPatient, req.getNomHopitalSource(), req.getNomHopitalDestination());
        return toResponse(transfert);
    }

    // Accepter un transfert
    @Transactional
    public TransfertResponse accepter(Long id) {
        Transfert transfert = find(id);

        if (transfert.getStatut() != StatutTransfert.EN_ATTENTE) {
            throw BusinessException.badRequest(
                    "Seul un transfert en attente peut être accepté.");
        }

        transfert.setStatut(StatutTransfert.ACCEPTE);
        transfertRepository.save(transfert);
        log.info("Transfert accepté : id={}", id);
        return toResponse(transfert);
    }

    // Refuser un transfert
    @Transactional
    public TransfertResponse refuser(Long id) {
        Transfert transfert = find(id);

        if (transfert.getStatut() != StatutTransfert.EN_ATTENTE) {
            throw BusinessException.badRequest(
                    "Seul un transfert en attente peut être refusé.");
        }

        transfert.setStatut(StatutTransfert.REFUSE);
        transfertRepository.save(transfert);
        log.info("Transfert refusé : id={}", id);
        return toResponse(transfert);
    }

    // Marquer comme effectué
    @Transactional
    public TransfertResponse marquerEffectue(Long id) {
        Transfert transfert = find(id);

        if (transfert.getStatut() != StatutTransfert.ACCEPTE) {
            throw BusinessException.badRequest(
                    "Seul un transfert accepté peut être marqué comme effectué.");
        }

        transfert.setStatut(StatutTransfert.EFFECTUE);
        transfertRepository.save(transfert);
        log.info("Transfert effectué : id={}", id);
        return toResponse(transfert);
    }

    // Annuler un transfert
    @Transactional
    public TransfertResponse annuler(Long id) {
        Transfert transfert = find(id);

        if (transfert.getStatut() == StatutTransfert.EFFECTUE) {
            throw BusinessException.badRequest(
                    "Un transfert déjà effectué ne peut pas être annulé.");
        }

        transfert.setStatut(StatutTransfert.ANNULE);
        transfertRepository.save(transfert);
        log.info("Transfert annulé : id={}", id);
        return toResponse(transfert);
    }

    // Lister les transferts d'un patient
    @Transactional(readOnly = true)
    public List<TransfertResponse> getByPatient(Long patientId) {
        return transfertRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lister par statut
    @Transactional(readOnly = true)
    public List<TransfertResponse> getByStatut(StatutTransfert statut) {
        return transfertRepository.findByStatut(statut)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'un transfert
    @Transactional(readOnly = true)
    public TransfertResponse getById(Long id) {
        return toResponse(find(id));
    }

    private Transfert find(Long id) {
        return transfertRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Transfert introuvable (id=" + id + ")"));
    }

    public TransfertResponse toResponse(Transfert t) {
        TransfertResponse.TransfertResponseBuilder b = TransfertResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .motif(t.getMotif())
                .compteRendu(t.getCompteRendu())
                .statut(t.getStatut())
                .dateTransfert(t.getDateTransfert())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt());

        if (t.getPatient() != null) {
            b.patientId(t.getPatient().getId())
                    .nomPatient(t.getPatient().getNom())
                    .prenomPatient(t.getPatient().getPrenom());
        }

        if (t.getMedecin() != null) {
            b.medecinId(t.getMedecin().getId())
                    .nomMedecin(t.getMedecin().getNom())
                    .prenomMedecin(t.getMedecin().getPrenom());
        }

        if (t.getHopitalSource() != null) {
            b.hopitalSource(t.getHopitalSource().getNom());
        }

        if (t.getHopitalDestination() != null) {
            b.hopitalDestination(t.getHopitalDestination().getNom());
        }

        return b.build();
    }
}
