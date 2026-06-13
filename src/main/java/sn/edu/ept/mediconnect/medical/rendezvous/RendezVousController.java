package sn.edu.ept.mediconnect.medical.rendezvous;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.common.entities.Role;
import sn.edu.ept.mediconnect.dtos.RendezVousRequest;
import sn.edu.ept.mediconnect.dtos.RendezVousResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous médicaux")
public class RendezVousController {

    private final RendezVousService rendezVousService;
    private final PatientRepository patientRepository;

    // GET /api/rendez-vous
    // Tous les rendez-vous — personnel soignant et admin
    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT', 'ADMIN')")
    @Operation(summary = "Lister tous les rendez-vous")
    public ResponseEntity<?> getAll() {
        List<RendezVousResponse> liste = rendezVousService.getAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // POST /api/rendez-vous   — Patient prend son propre rendez-vous
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Prendre un rendez-vous (patient connecté)")
    public ResponseEntity<?> createByPatient(
            @AuthenticationPrincipal User patientConnecte,
            @Valid @RequestBody RendezVousRequest req) {

        RendezVousResponse rdv = rendezVousService.create(
                patientConnecte.getId(), req.getMedecinId(), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Rendez-vous créé avec succès.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // POST /api/rendez-vous/patient/{numPatient}   — Personnel soignant crée un RDV pour un patient
    @PostMapping("/patient/{numPatient}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Créer un rendez-vous pour un patient (soignant)")
    public ResponseEntity<?> createByMedecin(
            @PathVariable String numPatient,
            @AuthenticationPrincipal User medecinConnecte,
            @Valid @RequestBody RendezVousRequest req) {

        Patient patient = patientRepository.findByNumPatient(numPatient)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable : " + numPatient));

        Long effectiveMedecinId;
        if (medecinConnecte.getRole() == Role.MEDECIN
                || medecinConnecte.getRole() == Role.CARDIOLOGUE) {
            effectiveMedecinId = medecinConnecte.getId();
        } else {
            effectiveMedecinId = req.getMedecinId();
        }

        RendezVousResponse rdv = rendezVousService.create(
                patient.getId(), effectiveMedecinId, req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Rendez-vous créé avec succès.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/rendez-vous/{id}
    // Patient → ne peut voir que ses propres RDV
    // Médecin / Cardiologue → peut voir tous les RDV
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Détail d'un rendez-vous")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte) {

        boolean isPatient = utilisateurConnecte.getRole() == Role.PATIENT;
        RendezVousResponse rdv = rendezVousService.getById(
                id, utilisateurConnecte.getId(), isPatient);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/rendez-vous/mes-rendez-vous
    // Patient connecté voit ses propres RDV
    @GetMapping("/mes-rendez-vous")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Mes rendez-vous")
    public ResponseEntity<?> getMesRendezVous(
            @AuthenticationPrincipal User patientConnecte) {

        List<RendezVousResponse> liste =
                rendezVousService.getByPatient(patientConnecte.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/rendez-vous/patient/{patientId}
    // Médecin / Cardiologue uniquement
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les rendez-vous d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<RendezVousResponse> liste = rendezVousService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/rendez-vous/medecin/{medecinId}
    // Médecin / Cardiologue uniquement
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les rendez-vous d'un médecin")
    public ResponseEntity<?> getByMedecin(@PathVariable Long medecinId) {
        List<RendezVousResponse> liste = rendezVousService.getByMedecin(medecinId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/confirmer
    @PatchMapping("/{id}/confirmer")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Confirmer un rendez-vous")
    public ResponseEntity<?> confirmer(@PathVariable Long id, @AuthenticationPrincipal User connectedUser) {
        Long medecinId = (connectedUser.getRole() == Role.MEDECIN
                || connectedUser.getRole() == Role.CARDIOLOGUE)
                ? connectedUser.getId() : null;
        RendezVousResponse rdv = rendezVousService.confirmer(id, medecinId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rendez-vous confirmé.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/annuler
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Annuler un rendez-vous")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        RendezVousResponse rdv = rendezVousService.annuler(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rendez-vous annulé.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/proposer-date — médecin propose une autre date
    @PatchMapping("/{id}/proposer-date")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Proposer une autre date pour un rendez-vous")
    public ResponseEntity<?> proposerDate(
            @PathVariable Long id,
            @AuthenticationPrincipal User connectedUser,
            @RequestBody Map<String, String> body) {

        String dateStr = body.get("dateProposee");
        if (dateStr == null || dateStr.isBlank()) {
            throw BusinessException.badRequest("Le champ 'dateProposee' est obligatoire.");
        }

        LocalDateTime nouvelleDate;
        try {
            nouvelleDate = LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            throw BusinessException.badRequest("Format de date invalide. Attendu : yyyy-MM-ddTHH:mm:ss");
        }

        Long medecinId = (connectedUser.getRole() == Role.MEDECIN
                || connectedUser.getRole() == Role.CARDIOLOGUE)
                ? connectedUser.getId() : null;

        RendezVousResponse rdv = rendezVousService.proposerDate(id, medecinId, nouvelleDate);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Date alternative proposée. En attente de confirmation du patient.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/accepter-proposition — patient accepte la date proposée
    @PatchMapping("/{id}/accepter-proposition")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Accepter la date proposée par le médecin")
    public ResponseEntity<?> accepterProposition(
            @PathVariable Long id,
            @AuthenticationPrincipal User patientConnecte) {
        RendezVousResponse rdv = rendezVousService.accepterProposition(id, patientConnecte.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rendez-vous confirmé à la nouvelle date.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/refuser-proposition — patient refuse la date proposée
    @PatchMapping("/{id}/refuser-proposition")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Refuser la date proposée par le médecin")
    public ResponseEntity<?> refuserProposition(
            @PathVariable Long id,
            @AuthenticationPrincipal User patientConnecte) {
        RendezVousResponse rdv = rendezVousService.refuserProposition(id, patientConnecte.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Date refusée. Le rendez-vous reste en attente.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/effectue
    @PatchMapping("/{id}/effectue")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Marquer un rendez-vous comme effectué")
    public ResponseEntity<?> marquerEffectue(@PathVariable Long id) {
        RendezVousResponse rdv = rendezVousService.marquerEffectue(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rendez-vous marqué comme effectué.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/rendez-vous/medecin/{medecinId}/creneaux
    // Retourne les N prochains créneaux disponibles à partir d'une date
    @GetMapping("/medecin/{medecinId}/creneaux")
    @PreAuthorize("hasAnyRole('PATIENT', 'MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT')")
    @Operation(summary = "Prochains créneaux disponibles pour un médecin")
    public ResponseEntity<?> getCreneauxDisponibles(
            @PathVariable Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(defaultValue = "5") int nbCreneaux) {

        List<LocalDateTime> creneaux = rendezVousService.getCreneauxDisponibles(
                medecinId, dateDebut, Math.min(nbCreneaux, 10));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", creneaux,
                "timestamp", LocalDateTime.now()
        ));
    }
}