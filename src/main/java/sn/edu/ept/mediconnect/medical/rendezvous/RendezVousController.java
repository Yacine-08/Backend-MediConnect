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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous médicaux")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // POST /api/rendez-vous
    // Patient → patientId depuis le token
    // Médecin → patientId obligatoire dans le request
    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Créer un rendez-vous")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User utilisateurConnecte,
            @Valid @RequestBody RendezVousRequest req) {

        Long patientId;

        if (utilisateurConnecte.getRole() == Role.PATIENT) {
            // Le patient prend son propre rendez-vous
            patientId = utilisateurConnecte.getId();
        } else {
            // Le médecin doit fournir le patientId
            if (req.getPatientId() == null) {
                throw BusinessException.badRequest(
                        "Le patientId est obligatoire pour un médecin.");
            }
            patientId = req.getPatientId();
        }

        RendezVousResponse rdv = rendezVousService.create(patientId, req);
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
    // Médecin / Cardiologue uniquement
    @PatchMapping("/{id}/confirmer")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Confirmer un rendez-vous")
    public ResponseEntity<?> confirmer(@PathVariable Long id) {
        RendezVousResponse rdv = rendezVousService.confirmer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rendez-vous confirmé.",
                "data", rdv,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/rendez-vous/{id}/annuler
    // Médecin / Cardiologue uniquement
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
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

    // PATCH /api/rendez-vous/{id}/effectue
    // Médecin / Cardiologue uniquement
    @PatchMapping("/{id}/effectue")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
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
}