package sn.edu.ept.mediconnect.common.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.entities.TypeEtablissement;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hopitaux")
@RequiredArgsConstructor
public class HopitalController {

    private final HopitalRepository hopitalRepo;

    /** GET /api/hopitaux — liste publique (utilisée pour les formulaires de saisie) */
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Map<String, Object>> hopitaux = hopitalRepo.findAll()
            .stream()
            .sorted((a, b) -> a.getNom().compareToIgnoreCase(b.getNom()))
            .map(this::toMap)
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "data", hopitaux));
    }

    /** POST /api/hopitaux — création d'un établissement (ADMIN uniquement) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String nom = (String) body.get("nom");
        if (nom == null || nom.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Le nom de l'établissement est obligatoire."));
        }

        Hopital hopital = Hopital.builder()
            .id(UUID.randomUUID().toString())
            .nom(nom.trim())
            .telephone((String) body.getOrDefault("telephone", null))
            .build();

        String type = (String) body.get("typeEtablissement");
        if (type != null && !type.isBlank()) {
            try { hopital.setTypeEtablissement(TypeEtablissement.valueOf(type)); }
            catch (IllegalArgumentException ignored) {}
        }

        Object lat = body.get("latitude");
        Object lon = body.get("longitude");
        if (lat instanceof Number) hopital.setLatitude(((Number) lat).floatValue());
        if (lon instanceof Number) hopital.setLongitude(((Number) lon).floatValue());

        Hopital saved = hopitalRepo.save(hopital);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "Établissement créé avec succès.",
            "data", toMap(saved)
        ));
    }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Hopital h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",  h.getId());
        m.put("nom", h.getNom());
        m.put("typeEtablissement", h.getTypeEtablissement() != null ? h.getTypeEtablissement().name() : null);
        m.put("telephone",  h.getTelephone());
        m.put("latitude",   h.getLatitude());
        m.put("longitude",  h.getLongitude());
        return m;
    }
}
