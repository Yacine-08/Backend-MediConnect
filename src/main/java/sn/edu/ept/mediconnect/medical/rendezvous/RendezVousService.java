package sn.edu.ept.mediconnect.medical.rendezvous;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.dtos.RendezVousRequest;
import sn.edu.ept.mediconnect.dtos.RendezVousResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.medecin.MedecinRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final HopitalRepository hopitalRepository;

    // Créer un rendez-vous — patientId vient soit du token soit du request
    @Transactional
    public RendezVousResponse create(Long patientId, RendezVousRequest req) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + patientId + ")"));

        Medecin medecin = medecinRepository.findById(req.getMedecinId())
                .orElseThrow(() -> BusinessException.notFound(
                        "Médecin introuvable (id=" + req.getMedecinId() + ")"));

        // Vérifier qu'il n'y a pas déjà un RDV au même créneau
        if (rendezVousRepository.existsByPatientIdAndMedecinIdAndDateHeure(
                patientId, req.getMedecinId(), req.getDateHeure())) {
            throw BusinessException.conflict(
                    "Un rendez-vous existe déjà pour ce créneau.");
        }

        RendezVous rendezVous = RendezVous.builder()
                .patient(patient)
                .medecin(medecin)
                .dateHeure(req.getDateHeure())
                .type(req.getType())
                .statut(StatutRendezVous.PLANIFIE)
                .motif(req.getMotif())
                .build();

        if (req.getNomHopital() != null) {
            Hopital hopital = hopitalRepository.findByNom(req.getNomHopital())
                    .orElseThrow(() -> BusinessException.notFound(
                            "Hôpital introuvable : " + req.getNomHopital()));
            rendezVous.setHopital(hopital);
        }

        rendezVousRepository.save(rendezVous);
        log.info("Rendez-vous créé : patient={} medecin={}", patientId, req.getMedecinId());
        return toResponse(rendezVous);
    }

    // Confirmer un rendez-vous
    @Transactional
    public RendezVousResponse confirmer(Long id) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() != StatutRendezVous.PLANIFIE) {
            throw BusinessException.badRequest(
                    "Seul un rendez-vous planifié peut être confirmé.");
        }

        rdv.setStatut(StatutRendezVous.CONFIRME);

        if (rdv.getType() == TypeRendezVous.VIDEO) {
            rdv.setLienVideo("https://meet.jit.si/mediconnect-" + UUID.randomUUID());
        }

        rendezVousRepository.save(rdv);
        log.info("Rendez-vous confirmé : id={}", id);
        return toResponse(rdv);
    }

    // Annuler un rendez-vous
    @Transactional
    public RendezVousResponse annuler(Long id) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() == StatutRendezVous.EFFECTUE) {
            throw BusinessException.badRequest(
                    "Un rendez-vous déjà effectué ne peut pas être annulé.");
        }

        rdv.setStatut(StatutRendezVous.ANNULE);
        rendezVousRepository.save(rdv);
        log.info("Rendez-vous annulé : id={}", id);
        return toResponse(rdv);
    }

    // Marquer comme effectué
    @Transactional
    public RendezVousResponse marquerEffectue(Long id) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() != StatutRendezVous.CONFIRME) {
            throw BusinessException.badRequest(
                    "Seul un rendez-vous confirmé peut être marqué comme effectué.");
        }

        rdv.setStatut(StatutRendezVous.EFFECTUE);
        rendezVousRepository.save(rdv);
        log.info("Rendez-vous effectué : id={}", id);
        return toResponse(rdv);
    }

    // Mes rendez-vous — patient connecté
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getByPatient(Long patientId) {
        return rendezVousRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Rendez-vous d'un médecin
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getByMedecin(Long medecinId) {
        return rendezVousRepository.findByMedecinId(medecinId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Détail d'un rendez-vous
    @Transactional(readOnly = true)
    public RendezVousResponse getById(Long id, Long utilisateurId, boolean isPatient) {
        RendezVous rdv = find(id);

        // Un patient ne peut voir que ses propres rendez-vous
        if (isPatient && !rdv.getPatient().getId().equals(utilisateurId)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez consulter que vos propres rendez-vous.");
        }

        return toResponse(rdv);
    }

    private RendezVous find(Long id) {
        return rendezVousRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Rendez-vous introuvable (id=" + id + ")"));
    }

    public RendezVousResponse toResponse(RendezVous r) {
        RendezVousResponse.RendezVousResponseBuilder b = RendezVousResponse.builder()
                .id(r.getId())
                .dateHeure(r.getDateHeure())
                .type(r.getType())
                .statut(r.getStatut())
                .motif(r.getMotif())
                .lienVideo(r.getLienVideo())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt());

        if (r.getPatient() != null) {
            b.patientId(r.getPatient().getId())
                    .nomPatient(r.getPatient().getNom())
                    .prenomPatient(r.getPatient().getPrenom());
        }

        if (r.getMedecin() != null) {
            b.medecinId(r.getMedecin().getId())
                    .nomMedecin(r.getMedecin().getNom())
                    .prenomMedecin(r.getMedecin().getPrenom());
        }

        if (r.getHopital() != null) {
            b.hopital(r.getHopital().getNom());
        }

        return b.build();
    }
}