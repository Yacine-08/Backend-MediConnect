package sn.edu.ept.mediconnect.common.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;
import sn.edu.ept.mediconnect.common.services.OrdreMedecinImportService;

import java.util.Map;

@RestController
@RequestMapping("/api/ordre-medecins")
@RequiredArgsConstructor
public class OrdreMedecinController {

    private final OrdreMedecinRepository ordreRepo;
    private final OrdreMedecinImportService importService;

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String numOrdre) {
        return ordreRepo.findByNumOrdre(numOrdre.trim().toUpperCase())
            .map(o -> ResponseEntity.ok(Map.of(
                "success",      true,
                "nom",          o.getNom(),
                "prenom",       o.getPrenom(),
                "section",      o.getSection()      != null ? o.getSection()      : "",
                "specialite",   o.getSpecialite()   != null ? o.getSpecialite()   : "",
                "mail",         o.getMail()         != null ? o.getMail()         : "",
                "localisation", o.getLocalisation() != null ? o.getLocalisation() : ""
            )))
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Numéro d'ordre introuvable"
            )));
    }

    /** Force une re-synchronisation depuis ordremedecins.sn (sans redémarrer le backend). */
    @PostMapping("/sync")
    public ResponseEntity<?> sync() {
        try {
            importService.synchroniser();
            return ResponseEntity.ok(Map.of("success", true, "message", "Synchronisation terminée"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
