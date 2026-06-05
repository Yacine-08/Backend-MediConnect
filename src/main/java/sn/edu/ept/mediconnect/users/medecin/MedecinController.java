package sn.edu.ept.mediconnect.users.medecin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.dtos.MedecinRequest;
import sn.edu.ept.mediconnect.dtos.MedecinResponse;
import sn.edu.ept.mediconnect.dtos.MedecinUpdateRequest;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
@Tag(name = "Médecins", description = "Gestion du profil médecin")
public class MedecinController {

    private final MedecinService medecinService;

    // GET /api/medecins
    // ADMIN : tous les médecins, avec filtres optionnels
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister les médecins (ADMIN)",
               description = "Filtres optionnels : ?actif=true|false  /  ?q=terme de recherche")
    public ResponseEntity<List<MedecinResponse>> lister(
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) String q) {

        List<MedecinResponse> liste;

        if (q != null && !q.isBlank()) {
            liste = medecinService.search(q.trim());
        } else if (Boolean.TRUE.equals(actif)) {
            liste = medecinService.getActifs();
        } else {
            liste = medecinService.getAll();
        }

        return ResponseEntity.ok(liste);
    }

    // GET /api/medecins/{id}
    // ADMIN : n'importe quel médecin
    // MEDECIN : uniquement son propre profil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDECIN')")
    @Operation(summary = "Détail d'un médecin")
    public ResponseEntity<MedecinResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte) {

        // Un médecin ne peut consulter que son propre profil
        if (utilisateurConnecte instanceof Medecin
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez consulter que votre propre profil.");
        }

        return ResponseEntity.ok(medecinService.getById(id));
    }

    // GET /api/medecins/me
    // Raccourci pour le médecin connecté
    @GetMapping("/me")
    @PreAuthorize("hasRole('MEDECIN')")
    @Operation(summary = "Mon profil médecin")
    public ResponseEntity<MedecinResponse> monProfil(
            @AuthenticationPrincipal User utilisateurConnecte) {
        return ResponseEntity.ok(medecinService.getById(utilisateurConnecte.getId()));
    }

    // PUT /api/medecins/{id}
    // ADMIN : peut modifier n'importe quel médecin
    // MEDECIN : peut modifier uniquement son propre profil
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDECIN')")
    @Operation(summary = "Mettre à jour un médecin")
    public ResponseEntity<?> mettreAJour(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte,
            @Valid @RequestBody MedecinUpdateRequest req) {

        // Un médecin ne peut modifier que son propre profil
        if (utilisateurConnecte instanceof Medecin
                && !utilisateurConnecte.getId().equals(id)) {
            throw BusinessException.forbidden(
                    "Vous ne pouvez modifier que votre propre profil.");
        }

        MedecinResponse medecin = medecinService.update(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil mis à jour avec succès.",
                "data", medecin,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/medecins/{id}/activer — ADMIN uniquement
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer le compte d'un médecin (ADMIN)")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        MedecinResponse medecin = medecinService.activate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte médecin activé.",
                "data", medecin,
                "timestamp", LocalDateTime.now()
        ));
    }

    // PATCH /api/medecins/{id}/desactiver — ADMIN uniquement
    @PatchMapping("/{id}/desactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver le compte d'un médecin (ADMIN)")
    public ResponseEntity<?> desactivate(@PathVariable Long id) {
        MedecinResponse medecin = medecinService.desactivate(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compte médecin désactivé.",
                "data", medecin,
                "timestamp", LocalDateTime.now()
        ));
    }

    // DELETE /api/medecins/{id} — ADMIN uniquement (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un médecin (soft delete, ADMIN)")
    public ResponseEntity<?> supprimer(@PathVariable Long id) {
        medecinService.supprimer(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Médecin désactivé (suppression logique).",
                "timestamp", LocalDateTime.now()
        ));
    }
}
