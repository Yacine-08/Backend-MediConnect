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

    // GET /api/medecins/search?q=terme — recherche pour tous les rôles soignants (initiation de transfert)
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE', 'INFIRMIER', 'ASSISTANT', 'ADMIN')")
    @Operation(summary = "Rechercher des médecins par nom/prénom (tous rôles soignants)")
    public ResponseEntity<List<MedecinResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(medecinService.search(q.trim()));
    }

    // GET /api/medecins/disponibles — médecins disponibles pour prise de RDV
    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('PATIENT', 'ASSISTANT', 'INFIRMIER', 'ADMIN', 'MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Médecins disponibles pour prise de RDV")
    public ResponseEntity<List<MedecinResponse>> getDisponibles() {
        return ResponseEntity.ok(medecinService.getDisponibles());
    }

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
    // Raccourci pour le médecin ou cardiologue connecté
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Mon profil médecin")
    public ResponseEntity<MedecinResponse> monProfil(
            @AuthenticationPrincipal User utilisateurConnecte) {
        return ResponseEntity.ok(medecinService.getById(utilisateurConnecte.getId()));
    }

    // PATCH /api/medecins/me/disponibilite — médecin bascule sa propre disponibilité
    @PatchMapping("/me/disponibilite")
    @PreAuthorize("hasAnyRole('MEDECIN', 'CARDIOLOGUE')")
    @Operation(summary = "Basculer ma disponibilité (MEDECIN)")
    public ResponseEntity<?> toggleDisponibilite(
            @AuthenticationPrincipal User utilisateurConnecte) {
        MedecinResponse medecin = medecinService.toggleDisponibilite(utilisateurConnecte.getId());
        String msg = Boolean.TRUE.equals(medecin.getDisponible())
                ? "Vous êtes maintenant disponible pour les rendez-vous."
                : "Vous n'êtes plus disponible pour les rendez-vous.";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", msg,
                "data", medecin,
                "timestamp", LocalDateTime.now()
        ));
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

    // PATCH /api/medecins/{id}/valider — ADMIN : valider le profil d'un médecin
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Valider le profil d'un médecin (ADMIN)")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        MedecinResponse medecin = medecinService.valider(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil médecin validé. Le médecin peut maintenant se connecter.",
                "data", medecin,
                "timestamp", LocalDateTime.now()
        ));
    }

    // GET /api/medecins/en-attente — ADMIN : médecins en attente de validation
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Médecins en attente de validation (ADMIN)")
    public ResponseEntity<?> getEnAttente() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", medecinService.getEnAttente(),
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
