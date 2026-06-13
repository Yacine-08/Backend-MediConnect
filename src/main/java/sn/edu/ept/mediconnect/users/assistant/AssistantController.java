package sn.edu.ept.mediconnect.users.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.AssistantRequest;
import sn.edu.ept.mediconnect.dtos.AssistantResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistants")
@RequiredArgsConstructor
@Tag(name = "Assistants", description = "Gestion du profil assistant médical")
public class AssistantController {

    private final AssistantService assistantService;

    // GET /api/assistants — ADMIN uniquement
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les assistants (ADMIN)")
    public ResponseEntity<List<AssistantResponse>> lister(
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q) {

        List<AssistantResponse> liste;
        if (q != null && !q.isBlank()) {
            liste = assistantService.search(q.trim());
        } else if (Boolean.TRUE.equals(actif)) {
            liste = assistantService.getActifs();
        } else {
            liste = assistantService.getAll();
        }
        return ResponseEntity.ok(liste);
    }

    // GET /api/assistants/me — ASSISTANT connecté
    @GetMapping("/me")
    @PreAuthorize("hasRole('ASSISTANT')")
    @Operation(summary = "Mon profil assistant")
    public ResponseEntity<AssistantResponse> monProfil(
            @AuthenticationPrincipal User utilisateurConnecte) {
        return ResponseEntity.ok(assistantService.getById(utilisateurConnecte.getId()));
    }

    // GET /api/assistants/{id} — ADMIN ou ASSISTANT (son propre profil)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    @Operation(summary = "Détail d'un assistant")
    public ResponseEntity<AssistantResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte) {

        if (utilisateurConnecte instanceof Assistant
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez consulter que votre propre profil.");
        }
        return ResponseEntity.ok(assistantService.getById(id));
    }

    // PUT /api/assistants/{id} — ADMIN ou ASSISTANT (son propre profil)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    @Operation(summary = "Mettre à jour un assistant")
    public ResponseEntity<?> mettreAJour(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte,
            @Valid @RequestBody AssistantRequest req) {

        if (utilisateurConnecte instanceof Assistant
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que votre propre profil.");
        }

        AssistantResponse assistant = assistantService.update(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil mis à jour avec succès.",
                "data", assistant,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/assistants/{id}/activate — ADMIN uniquement
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer le compte d'un assistant (ADMIN)")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        AssistantResponse assistant = assistantService.activate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte assistant activé.",
                "data", assistant,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/assistants/{id}/desactivate — ADMIN uniquement
    @PatchMapping("/{id}/desactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver le compte d'un assistant (ADMIN)")
    public ResponseEntity<?> desactivate(@PathVariable Long id) {
        AssistantResponse assistant = assistantService.deactivate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte assistant désactivé.",
                "data", assistant,
                "timestamp", LocalDateTime.now()
        ));
    }

    // DELETE /api/assistants/{id} — ADMIN uniquement (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un assistant (soft delete, ADMIN)")
    public ResponseEntity<?> supprimer(@PathVariable Long id) {
        assistantService.supprimer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Assistant désactivé (suppression logique).",
                "timestamp", LocalDateTime.now()
        ));
    }
}
