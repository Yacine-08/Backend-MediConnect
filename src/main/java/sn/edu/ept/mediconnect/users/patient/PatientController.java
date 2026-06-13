package sn.edu.ept.mediconnect.users.patient;

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
import sn.edu.ept.mediconnect.dtos.CreatePatientRequest;
import sn.edu.ept.mediconnect.dtos.CreatePatientResponse;
import sn.edu.ept.mediconnect.dtos.PatientResponse;
import sn.edu.ept.mediconnect.dtos.UpdatePatientRequest;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Gestion des patients — création par l'assistant médical")
public class PatientController {

    private final PatientService patientService;

    // POST /api/patients — L'ASSISTANT crée le compte du patient
    @PostMapping
    @PreAuthorize("hasAnyRole('ASSISTANT', 'INFIRMIER')")
    @Operation(
        summary = "Créer un patient",
        description = "L'assistant médical ou l'infirmier connecté ouvre un compte pour un patient. "
                    + "Un mot de passe temporaire est généré et retourné dans la réponse."
    )
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User createur,
            @Valid @RequestBody CreatePatientRequest req) {

        CreatePatientResponse response =
                patientService.create(createur.getId(), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", response,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/patients/me — Le patient connecté consulte son propre profil
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Profil du patient connecté")
    public ResponseEntity<PatientResponse> getMe(
            @AuthenticationPrincipal User patientConnecte) {
        return ResponseEntity.ok(patientService.getById(patientConnecte.getId()));
    }

    // GET /api/patients — Liste des patients selon le rôle
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT', 'INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les patients",
               description = "ADMIN/MEDECIN/INFIRMIER : tous. ASSISTANT : ses propres patients. Filtre optionnel : ?q=terme")
    public ResponseEntity<List<PatientResponse>> getAll(
            @AuthenticationPrincipal User utilisateurConnecte,
            @RequestParam(required = false) String q) {

        List<PatientResponse> liste;

        if (q != null && !q.isBlank()) {
            liste = patientService.search(q.trim());
        } else if (utilisateurConnecte.getRole() == Role.ASSISTANT) {
            liste = patientService.getByCreateur(utilisateurConnecte.getId());
        } else {
            liste = patientService.getAll();
        }

        return ResponseEntity.ok(liste);
    }

    // GET /api/patients/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT', 'INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Détail d'un patient")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    // PUT /api/patients/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Mettre à jour les informations d'un patient")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest req) {

        PatientResponse patient = patientService.update(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Patient mis à jour avec succès.",
                "data", patient,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/patients/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
    @Operation(summary = "Activer le compte d'un patient")
    public ResponseEntity<?> activer(@PathVariable Long id) {
        PatientResponse patient = patientService.activate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte patient activé.",
                "data", patient,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/patients/{id}/desactiver
    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
    @Operation(summary = "Désactiver le compte d'un patient")
    public ResponseEntity<?> desactiver(@PathVariable Long id) {
        PatientResponse patient = patientService.desactivate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte patient désactivé.",
                "data", patient,
                "timestamp", LocalDateTime.now()
        ));
    }

    // POST /api/patients/moi/demande-suppression — RGPD : patient demande la suppression
    @PostMapping("/moi/demande-suppression")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Demande de suppression de compte (RGPD)")
    public ResponseEntity<?> demanderSuppression(
            @AuthenticationPrincipal User patientConnecte) {
        patientService.demanderSuppression(patientConnecte.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Votre demande a été enregistrée. L'administration MediConnect traitera votre demande sous 30 jours ouvrables, conformément à la loi n°2008-12.",
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/patients/moi/mes-donnees — RGPD : export des données personnelles
    @GetMapping("/moi/mes-donnees")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Export des données personnelles (RGPD)")
    public ResponseEntity<?> exportMesDonnees(
            @AuthenticationPrincipal User patientConnecte) {
        java.util.Map<String, Object> donnees = patientService.exportDonnees(patientConnecte.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", donnees,
                "timestamp", LocalDateTime.now()
        ));
    }
}
