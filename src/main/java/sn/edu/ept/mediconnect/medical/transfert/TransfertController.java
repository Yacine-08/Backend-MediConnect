package sn.edu.ept.mediconnect.medical.transfert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.TransfertRequest;
import sn.edu.ept.mediconnect.dtos.TransfertResponse;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transferts")
@RequiredArgsConstructor
@Tag(name = "Transferts", description = "Gestion des transferts de patients entre hôpitaux")
public class TransfertController {

    private final TransfertService transfertService;
    private final PatientRepository patientRepository;

    // POST /api/transferts
    // Médecin / Cardiologue initie un transfert
    @PostMapping("/{numPatient}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Initier un transfert de patient")
    public ResponseEntity<?> create(
            @PathVariable String numPatient,
            @AuthenticationPrincipal User medecinConnecte,
            @Valid @RequestBody TransfertRequest req) {

        TransfertResponse transfert = transfertService.create(
                numPatient, medecinConnecte.getId(), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Transfert initié avec succès.",
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/transferts/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Détail d'un transfert")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        TransfertResponse transfert = transfertService.getById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/transferts/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Transferts d'un patient")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        List<TransfertResponse> liste = transfertService.getByPatient(patientId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/transferts/statut/{statut}
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Transferts par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutTransfert statut) {
        List<TransfertResponse> liste = transfertService.getByStatut(statut);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", liste,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/transferts/{id}/accepter
    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Accepter un transfert")
    public ResponseEntity<?> accepter(@PathVariable Long id) {
        TransfertResponse transfert = transfertService.accepter(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transfert accepté.",
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/transferts/{id}/refuser
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Refuser un transfert")
    public ResponseEntity<?> refuser(@PathVariable Long id) {
        TransfertResponse transfert = transfertService.refuser(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transfert refusé.",
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/transferts/{id}/effectue
    @PatchMapping("/{id}/effectue")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Marquer un transfert comme effectué")
    public ResponseEntity<?> marquerEffectue(@PathVariable Long id) {
        TransfertResponse transfert = transfertService.marquerEffectue(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transfert effectué.",
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/transferts/{id}/annuler
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'ADMIN')")
    @Operation(summary = "Annuler un transfert")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        TransfertResponse transfert = transfertService.annuler(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transfert annulé.",
                "data", transfert,
                "timestamp", LocalDateTime.now()
        ));
    }
}