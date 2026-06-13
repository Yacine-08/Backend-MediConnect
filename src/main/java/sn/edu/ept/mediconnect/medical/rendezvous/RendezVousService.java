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

import java.time.Duration;
import java.util.ArrayList;
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
    public RendezVousResponse create(Long patientId, Long medecinId, RendezVousRequest req) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + patientId + ")"));

        RendezVous rendezVous = RendezVous.builder()
                .patient(patient)
                .dateHeure(req.getDateHeure())
                .type(req.getType())
                .statut(StatutRendezVous.PLANIFIE)
                .motif(req.getMotif())
                .build();

        // Médecin optionnel — null si c'est le patient qui crée
        if (medecinId != null) {
            Medecin medecin = medecinRepository.findById(medecinId)
                    .orElseThrow(() -> BusinessException.notFound(
                            "Médecin introuvable (id=" + medecinId + ")"));
            rendezVous.setMedecin(medecin);

            // Vérifier les conflits de créneaux (± 30 min)
            if (hasConflict(medecinId, req.getDateHeure())) {
                throw BusinessException.conflict(
                    "RDV_CONFLIT — Ce médecin a déjà un rendez-vous sur ce créneau. " +
                    "Consultez les créneaux disponibles via /api/rendez-vous/medecin/" + medecinId + "/creneaux");
            }
        }

        if (req.getNomHopital() != null) {
            Hopital hopital = hopitalRepository.findByNom(req.getNomHopital())
                    .orElseThrow(() -> BusinessException.notFound(
                            "Hôpital introuvable : " + req.getNomHopital()));
            rendezVous.setHopital(hopital);
        }

        // Lien Jitsi généré immédiatement pour les RDV vidéo
        if (req.getType() == TypeRendezVous.VIDEO) {
            rendezVous.setLienVideo("https://meet.jit.si/mediconnect-" + UUID.randomUUID());
        }

        rendezVousRepository.save(rendezVous);
        log.info("Rendez-vous créé : patient={}", patientId);
        return toResponse(rendezVous);
    }

    // Confirmer un rendez-vous
    @Transactional
    public RendezVousResponse confirmer(Long id, Long medecinId) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() != StatutRendezVous.PLANIFIE) {
            throw BusinessException.badRequest(
                    "Seul un rendez-vous planifié peut être confirmé.");
        }

        // On assigne le médecin si c'est un médecin/cardiologue qui confirme
        if (medecinId != null) {
            Medecin medecin = medecinRepository.findById(medecinId)
                    .orElseThrow(() -> BusinessException.notFound(
                            "Médecin introuvable (id=" + medecinId + ")"));
            rdv.setMedecin(medecin);
        }
        rdv.setStatut(StatutRendezVous.CONFIRME);

        if (rdv.getType() == TypeRendezVous.VIDEO) {
            rdv.setLienVideo("https://meet.jit.si/mediconnect-" + UUID.randomUUID());
        }

        rendezVousRepository.save(rdv);
        log.info("Rendez-vous confirmé par médecin id={}", medecinId);
        return toResponse(rdv);
    }

    // Médecin propose une autre date
    @Transactional
    public RendezVousResponse proposerDate(Long id, Long medecinId, java.time.LocalDateTime nouvelleDate) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() == StatutRendezVous.ANNULE || rdv.getStatut() == StatutRendezVous.EFFECTUE) {
            throw BusinessException.badRequest(
                    "Impossible de proposer une date pour un rendez-vous annulé ou effectué.");
        }
        if (medecinId != null && rdv.getMedecin() != null
                && !rdv.getMedecin().getId().equals(medecinId)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que vos propres rendez-vous.");
        }

        rdv.setDateProposee(nouvelleDate);
        rdv.setStatut(StatutRendezVous.DATE_PROPOSEE);
        rendezVousRepository.save(rdv);
        log.info("Date alternative proposée pour RDV id={} : {}", id, nouvelleDate);
        return toResponse(rdv);
    }

    // Patient accepte la date proposée → devient la nouvelle dateHeure, statut CONFIRME
    @Transactional
    public RendezVousResponse accepterProposition(Long id, Long patientId) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() != StatutRendezVous.DATE_PROPOSEE) {
            throw BusinessException.badRequest(
                    "Aucune proposition de date en attente pour ce rendez-vous.");
        }
        if (patientId != null && !rdv.getPatient().getId().equals(patientId)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que vos propres rendez-vous.");
        }

        rdv.setDateHeure(rdv.getDateProposee());
        rdv.setDateProposee(null);
        rdv.setStatut(StatutRendezVous.CONFIRME);

        if (rdv.getType() == TypeRendezVous.VIDEO) {
            rdv.setLienVideo("https://meet.jit.si/mediconnect-" + java.util.UUID.randomUUID());
        }

        rendezVousRepository.save(rdv);
        log.info("Patient id={} a accepté la date proposée pour RDV id={}", patientId, id);
        return toResponse(rdv);
    }

    // Patient refuse la date proposée → retour à PLANIFIE, dateProposee effacée
    @Transactional
    public RendezVousResponse refuserProposition(Long id, Long patientId) {
        RendezVous rdv = find(id);

        if (rdv.getStatut() != StatutRendezVous.DATE_PROPOSEE) {
            throw BusinessException.badRequest(
                    "Aucune proposition de date en attente pour ce rendez-vous.");
        }
        if (patientId != null && !rdv.getPatient().getId().equals(patientId)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que vos propres rendez-vous.");
        }

        rdv.setDateProposee(null);
        rdv.setStatut(StatutRendezVous.PLANIFIE);
        rendezVousRepository.save(rdv);
        log.info("Patient id={} a refusé la date proposée pour RDV id={}", patientId, id);
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

    // Tous les rendez-vous (personnel soignant, admin)
    @Transactional(readOnly = true)
    public List<RendezVousResponse> getAll() {
        return rendezVousRepository.findAll()
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

    private boolean hasConflict(Long medecinId, java.time.LocalDateTime dateHeure) {
        java.time.LocalDateTime debut = dateHeure.minusMinutes(29);
        java.time.LocalDateTime fin   = dateHeure.plusMinutes(29);
        return rendezVousRepository.existsByMedecinIdAndDateHeureBetweenAndStatutIn(
                medecinId, debut, fin,
                List.of(StatutRendezVous.PLANIFIE, StatutRendezVous.CONFIRME));
    }

    @Transactional(readOnly = true)
    public List<java.time.LocalDateTime> getCreneauxDisponibles(
            Long medecinId, java.time.LocalDateTime dateDebut, int nbCreneaux) {

        List<RendezVous> rdvExistants = rendezVousRepository
                .findByMedecinIdAndDateHeureAfterAndStatutInOrderByDateHeureAsc(
                        medecinId, dateDebut.minusMinutes(30),
                        List.of(StatutRendezVous.PLANIFIE, StatutRendezVous.CONFIRME));

        List<java.time.LocalDateTime> creneaux = new ArrayList<>();
        java.time.LocalDateTime candidat = dateDebut;
        int maxIterations = nbCreneaux * 20;

        while (creneaux.size() < nbCreneaux && maxIterations-- > 0) {
            final java.time.LocalDateTime c = candidat;
            boolean occupe = rdvExistants.stream().anyMatch(r ->
                    Math.abs(Duration.between(r.getDateHeure(), c).toMinutes()) < 30);
            if (!occupe) creneaux.add(c);
            candidat = candidat.plusMinutes(30);
            // Sauter les heures hors plage 08h00-18h00
            if (candidat.getHour() >= 18) {
                candidat = candidat.plusDays(1)
                        .withHour(8).withMinute(0).withSecond(0).withNano(0);
            }
        }

        return creneaux;
    }

    public RendezVousResponse toResponse(RendezVous r) {
        RendezVousResponse.RendezVousResponseBuilder b = RendezVousResponse.builder()
                .id(r.getId())
                .dateHeure(r.getDateHeure())
                .type(r.getType())
                .statut(r.getStatut())
                .motif(r.getMotif())
                .lienVideo(r.getLienVideo())
                .dateProposee(r.getDateProposee())
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
                    .prenomMedecin(r.getMedecin().getPrenom())
                    .specialiteMedecin(r.getMedecin().getSpecialite());
        }

        if (r.getHopital() != null) {
            b.hopital(r.getHopital().getNom());
        }

        return b.build();
    }
}