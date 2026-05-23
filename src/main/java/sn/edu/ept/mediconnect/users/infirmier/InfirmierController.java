package sn.edu.ept.mediconnect.users.infirmier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.InfirmierRequest;
import sn.edu.ept.mediconnect.dtos.InfirmierResponse;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/infirmiers")
@RequiredArgsConstructor
@Tag(name = "Infirmiers", description = "Gestion du profil infirmier")
public class InfirmierController {

    private final InfirmierService infirmierService;

    //  GET /api/infirmiers
    //  ADMIN : tous les infirmiers, avec filtres optionnels
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les infirmiers (ADMIN)",
               description = "Filtres optionnels : ?actif=true|false  /  ?q=terme de recherche")
    public ResponseEntity<List<InfirmierResponse>> lister(
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q) {

        List<InfirmierResponse> liste;

        if (q != null && !q.isBlank()) {
            liste = infirmierService.search(q.trim());
        } else if (Boolean.TRUE.equals(actif)) {
            liste = infirmierService.getActifs();
        } else {
            liste = infirmierService.getAll();
        }

        return ResponseEntity.ok(liste);
    }

    //  GET /api/infirmiers/{id}
    //  ADMIN : n'importe quel infirmier
    //  INFIRMIER : uniquement son propre profil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INFIRMIER')")
    @Operation(summary = "Détail d'un infirmier")
    public ResponseEntity<InfirmierResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte) {

        // Un infirmier ne peut consulter que son propre profil
        if (utilisateurConnecte instanceof Infirmier
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez consulter que votre propre profil.");
        }

        return ResponseEntity.ok(infirmierService.getById(id));
    }

    //  GET /api/infirmiers/me
    //  Raccourci pour l'infirmier connecté
    @GetMapping("/me")
    @PreAuthorize("hasRole('INFIRMIER')")
    @Operation(summary = "Mon profil infirmier")
    public ResponseEntity<InfirmierResponse> monProfil(
            @AuthenticationPrincipal User utilisateurConnecte) {
        return ResponseEntity.ok(infirmierService.getById(utilisateurConnecte.getId()));
    }


    //  PUT /api/infirmiers/{id}
    //  ADMIN : peut modifier n'importe quel infirmier
    //  INFIRMIER : peut modifier uniquement son propre profil
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INFIRMIER')")
    @Operation(summary = "Mettre à jour un infirmier")
    public ResponseEntity<?> mettreAJour(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte,
            @Valid @RequestBody InfirmierRequest req) {

        // Un infirmier ne peut modifier que son propre profil
        if (utilisateurConnecte instanceof Infirmier
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que votre propre profil.");
        }

        InfirmierResponse infirmier = infirmierService.update(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil mis à jour avec succès.",
                "data", infirmier,
                "timestamp", LocalDateTime.now()
        ));
    }

    //  PATCH /api/infirmiers/{id}/activer — ADMIN uniquement
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer le compte d'un infirmier (ADMIN)")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        InfirmierResponse infirmier = infirmierService.activate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte infirmier activé.",
                "data", infirmier,
                "timestamp", LocalDateTime.now()
        ));
    }

    //  PATCH /api/infirmiers/{id}/desactiver — ADMIN uniquement
    @PatchMapping("/{id}/desactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver le compte d'un infirmier (ADMIN)")
    public ResponseEntity<?> desactivate(@PathVariable Long id) {
        InfirmierResponse infirmier = infirmierService.deactivate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte infirmier désactivé.",
                "data", infirmier,
                "timestamp", LocalDateTime.now()
        ));
    }


    //  DELETE /api/infirmiers/{id}  — ADMIN uniquement (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un infirmier (soft delete, ADMIN)")
    public ResponseEntity<?> supprimer(@PathVariable Long id) {
        infirmierService.supprimer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Infirmier désactivé (suppression logique).",
                "timestamp", LocalDateTime.now()
        ));
    }
}