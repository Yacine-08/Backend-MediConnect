package sn.edu.ept.mediconnect.medical.consultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.ConsultationMedecineRequest;
import sn.edu.ept.mediconnect.dtos.ConsultationRequest;
import sn.edu.ept.mediconnect.dtos.ConsultationResponse;
import sn.edu.ept.mediconnect.dtos.ConstantesRequest;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultations", description = "Gestion des consultations médicales")
public class ConsultationController {

    private final ConsultationService consultationService;

    // POST /api/consultations
    // Médecin / Cardiologue crée une consultation
    @PostMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Créer une consultation")
    public ResponseEntity<?> create(@Valid @RequestBody ConsultationRequest req) {
        ConsultationResponse consultation = consultationService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Consultation créée avec succès.",
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/consultations/{id}
    // Médecin, Cardiologue, Infirmier peuvent voir le détail
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Détail d'une consultation")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ConsultationResponse consultation = consultationService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/consultations/patient/{patientId}
    // Médecin / Cardiologue voient les consultations d'un patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Lister les consultations d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<ConsultationResponse> liste = consultationService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/consultations/medecin/{medecinId}
    // Médecin / Cardiologue voient leurs propres consultations
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Lister les consultations d'un médecin")
    public ResponseEntity<?> getByMedecin(@PathVariable Long medecinId) {
        List<ConsultationResponse> liste = consultationService.getByMedecin(medecinId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/consultations/{id}/constantes
    // Infirmier prend les constantes — Phase 1
    @PatchMapping("/{id}/constantes")
    @PreAuthorize("hasRole('INFIRMIER', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Prise des constantes vitales par l'infirmier")
    public ResponseEntity<?> prendreConstantes(
            @PathVariable Long id,
            @AuthenticationPrincipal User infirmierConnecte,
            @Valid @RequestBody ConstantesRequest req) {
        ConsultationResponse consultation = consultationService.prendreConstantes(
                id, infirmierConnecte.getId(), req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Constantes vitales enregistrées.",
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/consultations/{id}/completer
    // Médecin / Cardiologue complète la consultation — Phase 2
    @PatchMapping("/{id}/completer")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER')")
    @Operation(summary = "Compléter la consultation par le médecin")
    public ResponseEntity<?> completerConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationMedecineRequest req) {
        ConsultationResponse consultation = consultationService.completerConsultation(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Consultation complétée.",
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/consultations/{id}/terminer
    // Médecin / Cardiologue termine la consultation
    @PatchMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Terminer une consultation")
    public ResponseEntity<?> terminer(@PathVariable Long id) {
        ConsultationResponse consultation = consultationService.terminer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Consultation terminée.",
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/consultations/{id}/annuler
    // Médecin / Cardiologue annule la consultation
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Annuler une consultation")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        ConsultationResponse consultation = consultationService.annuler(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Consultation annulée.",
                "data", consultation,
                "timestamp", LocalDateTime.now()
        ));
    }
}