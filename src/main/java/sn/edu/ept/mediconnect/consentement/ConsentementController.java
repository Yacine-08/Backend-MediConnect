package sn.edu.ept.mediconnect.consentement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.ConsentementResponse;
import sn.edu.ept.mediconnect.dtos.UpdateConsentementRequest;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/patients/{patientId}/consentements")
@RequiredArgsConstructor
@Tag(name = "Consentements", description = "Gestion des consentements patients")
public class ConsentementController {

    private final ConsentementService consentementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','CARDIOLOGUE','INFIRMIER','PATIENT')")
    @Operation(
        summary = "Consulter les consentements d'un patient",
        description = "Un patient ne peut consulter que ses propres consentements."
    )
    public ResponseEntity<List<ConsentementResponse>> getConsentements(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User utilisateurConnecte) {

        verifyAccess(utilisateurConnecte, patientId);
        return ResponseEntity.ok(consentementService.getConsentements(patientId));
    }

    @PutMapping("/{type}")
    @PreAuthorize("hasAnyRole('MEDECIN','CARDIOLOGUE','INFIRMIER','PATIENT')")
    @Operation(
        summary = "Mettre à jour un consentement",
        description = "Accepte ou refuse UTILISATION_IA ou ENTRAINEMENT_MODELES. "
                    + "La POLITIQUE_CONFIDENTIALITE ne peut pas être modifiée après inscription. "
                    + "Accessible par le patient lui-même, un médecin ou un infirmier."
    )
    public ResponseEntity<?> updateConsentement(
            @PathVariable Long patientId,
            @PathVariable TypeConsentement type,
            @Valid @RequestBody UpdateConsentementRequest request,
            @AuthenticationPrincipal User utilisateurConnecte) {

        verifyAccess(utilisateurConnecte, patientId);

        ConsentementResponse reponse = consentementService.updateConsentement(
                patientId, type, request.getAccepte(), utilisateurConnecte);

        String decision = Boolean.TRUE.equals(request.getAccepte()) ? "accepté" : "refusé";

        return ResponseEntity.ok(Map.of(
                "success",   true,
                "message",   "Consentement " + type.name() + " " + decision + ".",
                "data",      reponse,
                "timestamp", LocalDateTime.now()
        ));
    }

    private void verifyAccess(User utilisateur, Long patientId) {
        if ("PATIENT".equals(utilisateur.getRole().name())
                && !utilisateur.getId().equals(patientId)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez accéder qu'à vos propres consentements.");
        }
    }
}