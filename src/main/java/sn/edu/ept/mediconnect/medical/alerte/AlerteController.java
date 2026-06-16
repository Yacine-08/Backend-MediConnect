package sn.edu.ept.mediconnect.medical.alerte;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.AlerteRequest;
import sn.edu.ept.mediconnect.dtos.AlerteResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
@Tag(name = "Alertes", description = "Gestion des alertes cliniques")
public class AlerteController {

    private final AlerteService alerteService;

    // POST /api/alertes — Créer une alerte clinique
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Créer une alerte clinique")
    public ResponseEntity<?> create(@Valid @RequestBody AlerteRequest req) {
        AlerteResponse alerte = alerteService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Alerte créée avec succès.",
                "data", alerte,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/alertes/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Détail d'une alerte")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        AlerteResponse alerte = alerteService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", alerte,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/alertes/non-acquittees
    // Toutes les alertes en attente
    @GetMapping("/non-acquittees")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Lister les alertes non acquittées")
    public ResponseEntity<?> getNonAcquittees() {
        List<AlerteResponse> liste = alerteService.getNonAcquittees();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/alertes/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Alertes d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<AlerteResponse> liste = alerteService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/alertes/consultation/{consultationId}
    @GetMapping("/consultation/{consultationId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Alertes d'une consultation")
    public ResponseEntity<?> getByConsultation(@PathVariable Long consultationId) {
        List<AlerteResponse> liste = alerteService.getByConsultation(consultationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/alertes/niveau/{niveau}
    @GetMapping("/niveau/{niveau}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Alertes par niveau")
    public ResponseEntity<?> getByNiveau(@PathVariable NiveauAlerte niveau) {
        List<AlerteResponse> liste = alerteService.getByNiveau(niveau);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/alertes/{id}/acquitter
    // Médecin / Cardiologue / Infirmier acquitte une alerte
    @PatchMapping("/{id}/acquitter")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Acquitter une alerte")
    public ResponseEntity<?> acquitter(@PathVariable Long id) {
        AlerteResponse alerte = alerteService.acquitter(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alerte acquittée avec succès.",
                "data", alerte,
                "timestamp", LocalDateTime.now()
        ));
    }
}