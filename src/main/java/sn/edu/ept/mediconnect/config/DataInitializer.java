// À créer : DataInitializer.java
package sn.edu.ept.mediconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;
import sn.edu.ept.mediconnect.common.services.OrdreMedecinImportService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final OrdreMedecinImportService ordreMedecinImportService;
    private final OrdreMedecinRepository    ordreMedecinRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Importer l'Ordre des Médecins uniquement si la table est vide
        if (ordreMedecinRepository.count() == 0) {
            log.info("Importation du tableau de l'Ordre des Médecins...");
            try {
                ordreMedecinImportService.importer();
                log.info("Importation terminée : {} médecins chargés",
                    ordreMedecinRepository.count());
            } catch (Exception e) {
                log.warn("Importation Ordre échouée (réseau?) : {}. " +
                    "La vérification manuelle sera désactivée.", e.getMessage());
            }
        } else {
            log.info("Tableau Ordre des Médecins déjà chargé ({} entrées)",
                ordreMedecinRepository.count());
        }
    }
}