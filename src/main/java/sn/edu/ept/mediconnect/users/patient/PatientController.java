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
import sn.edu.ept.mediconnect.dtos.CreatePatientRequest;
import sn.edu.ept.mediconnect.dtos.CreatePatientResponse;
import sn.edu.ept.mediconnect.dtos.PatientResponse;
import sn.edu.ept.mediconnect.dtos.UpdatePatientRequest;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Gestion des patients — création par l'infirmier")
public class PatientController {

    private final PatientService patientService;


    //  POST /api/patients
    //  L'infirmier authentifié crée le compte du patient
    @PostMapping
    @PreAuthorize("hasRole('INFIRMIER')")
    @Operation(
        summary = "Créer un patient",
        description = "L'infirmier connecté ouvre un compte pour un patient. "
                    + "Un mot de passe temporaire est généré et retourné dans la réponse."
    )
    public ResponseEntity<?> create(
            @AuthenticationPrincipal User infirmierConnecte,
            @Valid @RequestBody CreatePatientRequest req) {

        CreatePatientResponse response =
                patientService.create(infirmierConnecte.getId(), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", response,
                "timestamp", LocalDateTime.now()
        ));
    }


    //  GET /api/patients
    //  - ADMIN : tous les patients
    //  - INFIRMIER : uniquement ses patients
    //  Paramètre optionnel : ?q=terme
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les patients",
               description = "ADMIN : tous. INFIRMIER : ses propres patients. Filtre optionnel : ?q=terme")
    public ResponseEntity<List<PatientResponse>> getAll(
            @AuthenticationPrincipal User utilisateurConnecte,
            @RequestParam(required = false) String q) {

        List<PatientResponse> liste;

        if (q != null && !q.isBlank()) {
            liste = patientService.search(q.trim());
        } else if (utilisateurConnecte instanceof Infirmier) {
            // L'infirmier ne voit que ses patients
            liste = patientService.getByInfirmier(utilisateurConnecte.getId());
        } else {
            // L'admin voit tout
            liste = patientService.getAll();
        }

        return ResponseEntity.ok(liste);
    }


    //  GET /api/patients/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Détail d'un patient")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }


    //  PUT /api/patients/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
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


    //  PATCH /api/patients/{id}/activate
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INFIRMIER')")
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

    //  PATCH /api/patients/{id}/desactiver
    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'INFIRMIER')")
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
}