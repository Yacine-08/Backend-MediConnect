package sn.edu.ept.mediconnect.medical.dossier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.DossierMedicalRequest;
import sn.edu.ept.mediconnect.dtos.DossierMedicalResponse;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@Tag(name = "Dossiers Médicaux", description = "Gestion des dossiers médicaux")
public class DossierMedicalController {

    private final DossierMedicalService dossierMedicalService;

    // POST /api/dossiers/patient/{patientId}
    // Créer manuellement un dossier si inexistant
    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ADMIN')")
    @Operation(summary = "Créer manuellement un dossier médical")
    public ResponseEntity<?> create(@PathVariable Long patientId) {
        DossierMedicalResponse dossier = dossierMedicalService.create(patientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", dossier,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/dossiers/patient/{patientId}
    // Médecin, Cardiologue, Infirmier, Admin et le Patient lui-même peuvent consulter
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ADMIN', 'ASSISTANT', 'PATIENT')")
    @Operation(summary = "Consulter le dossier médical d'un patient")
    public ResponseEntity<?> getByPatientId(@PathVariable Long patientId) {
        DossierMedicalResponse dossier = dossierMedicalService.getByPatientId(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dossier,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/dossiers/{id}
    // Médecin, Cardiologue, Infirmier, Admin peuvent consulter
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ADMIN', 'ASSISTANT')")
    @Operation(summary = "Consulter un dossier médical par son id")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        DossierMedicalResponse dossier = dossierMedicalService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dossier,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PUT /api/dossiers/patient/{patientId}
    // Uniquement Médecin et Cardiologue peuvent modifier
    @PutMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Mettre à jour le dossier médical d'un patient")
    public ResponseEntity<?> update(
            @PathVariable Long patientId,
            @Valid @RequestBody DossierMedicalRequest req) {
        DossierMedicalResponse dossier = dossierMedicalService.update(patientId, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dossier médical mis à jour avec succès.",
                "data", dossier,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/dossiers/patient/{patientId}/archiver
    // Uniquement Admin peut archiver
    @PatchMapping("/patient/{patientId}/archiver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archiver le dossier médical d'un patient")
    public ResponseEntity<?> archiver(@PathVariable Long patientId) {
        DossierMedicalResponse dossier = dossierMedicalService.archiver(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dossier médical archivé avec succès.",
                "data", dossier,
                "timestamp", LocalDateTime.now()
        ));
    }
}
