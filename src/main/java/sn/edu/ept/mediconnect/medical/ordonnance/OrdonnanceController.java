package sn.edu.ept.mediconnect.medical.ordonnance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.OrdonnanceRequest;
import sn.edu.ept.mediconnect.dtos.OrdonnanceResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordonnances")
@RequiredArgsConstructor
@Tag(name = "Ordonnances", description = "Gestion des ordonnances médicales")
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;

    // POST /api/ordonnances
    // Médecin / Cardiologue crée une ordonnance
    @PostMapping("/consultation/{consultationId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Créer une ordonnance")
    public ResponseEntity<?> create(
            @PathVariable Long consultationId,
            @Valid @RequestBody OrdonnanceRequest req) {

        OrdonnanceResponse ordonnance = ordonnanceService.create(consultationId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Ordonnance créée avec succès.",
                "data", ordonnance,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/ordonnances/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'PATIENT')")
    @Operation(summary = "Détail d'une ordonnance")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        OrdonnanceResponse ordonnance = ordonnanceService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", ordonnance,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/ordonnances/consultation/{consultationId}
    @GetMapping("/consultation/{consultationId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'PATIENT')")
    @Operation(summary = "Ordonnance d'une consultation")
    public ResponseEntity<?> getByConsultation(@PathVariable Long consultationId) {
        OrdonnanceResponse ordonnance = ordonnanceService.getByConsultation(consultationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", ordonnance,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/ordonnances/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'PATIENT')")
    @Operation(summary = "Ordonnances d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<OrdonnanceResponse> liste = ordonnanceService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/ordonnances/medecin/{medecinId}
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('MEDECIN','CARDIOLOGUE')")
    @Operation(summary = "Ordonnances d'un médecin")
    public ResponseEntity<?> getByMedecin(@PathVariable Long medecinId) {
        List<OrdonnanceResponse> liste = ordonnanceService.getByMedecin(medecinId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/ordonnances/{id}/signer
    // Médecin / Cardiologue signe l'ordonnance
    @PatchMapping("/{id}/signer")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Signer une ordonnance")
    public ResponseEntity<?> signer(@PathVariable Long id) {
        OrdonnanceResponse ordonnance = ordonnanceService.signer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ordonnance signée avec succès.",
                "data", ordonnance,
                "timestamp", LocalDateTime.now()
        ));
    }
}