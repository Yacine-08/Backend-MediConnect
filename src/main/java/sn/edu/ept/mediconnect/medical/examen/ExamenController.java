package sn.edu.ept.mediconnect.medical.examen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.ExamenRequest;
import sn.edu.ept.mediconnect.dtos.ExamenResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examens")
@RequiredArgsConstructor
@Tag(name = "Examens", description = "Gestion des examens médicaux")
public class ExamenController {

    private final ExamenService examenService;

    // POST /api/examens
    // Médecin / Cardiologue prescrit un examen
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Prescrire un examen")
    public ResponseEntity<?> create(@Valid @RequestBody ExamenRequest req) {
        ExamenResponse examen = examenService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Examen prescrit avec succès.",
                "data", examen,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/examens/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Détail d'un examen")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ExamenResponse examen = examenService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", examen,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/examens/consultation/{consultationId}
    @GetMapping("/consultation/{consultationId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Lister les examens d'une consultation")
    public ResponseEntity<?> getByConsultation(@PathVariable Long consultationId) {
        List<ExamenResponse> liste = examenService.getByConsultation(consultationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/examens/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les examens d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<ExamenResponse> liste = examenService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/examens/{id}/realiser
    // Marquer un examen comme réalisé
    @PatchMapping("/{id}/realiser")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Marquer un examen comme réalisé")
    public ResponseEntity<?> marquerRealise(
            @PathVariable Long id,
            @RequestParam(required = false) String fichierUrl,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Long tailleFichier) {
        ExamenResponse examen = examenService.marquerRealise(id, fichierUrl, format, tailleFichier);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Examen marqué comme réalisé.",
                "data", examen,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/examens/{id}/annuler
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Annuler un examen")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        ExamenResponse examen = examenService.annuler(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Examen annulé.",
                "data", examen,
                "timestamp", LocalDateTime.now()
        ));
    }
}