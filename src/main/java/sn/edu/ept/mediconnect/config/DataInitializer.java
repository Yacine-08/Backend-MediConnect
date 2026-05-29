package sn.edu.ept.mediconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;
import sn.edu.ept.mediconnect.common.services.OrdreMedecinImportService;
import sn.edu.ept.mediconnect.consentement.Consentement;
import sn.edu.ept.mediconnect.consentement.ConsentementRepository;
import sn.edu.ept.mediconnect.consentement.TypeConsentement;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final OrdreMedecinImportService ordreMedecinImportService;
    private final OrdreMedecinRepository    ordreMedecinRepository;
    private final ConsentementRepository    consentementRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initialiserTemplatesConsentements();
        importerOrdreMedecins();
    }

    private void initialiserTemplatesConsentements() {

        if (!consentementRepository.existsByTypeConsentement(
                TypeConsentement.POLITIQUE_CONFIDENTIALITE)) {
            consentementRepository.save(Consentement.builder()
                    .typeConsentement(TypeConsentement.POLITIQUE_CONFIDENTIALITE)
                    .titre("Politique de confidentialité et conditions d'utilisation")
                    .contenu(
                            "En créant un compte sur MediConnect, vous acceptez que vos données " +
                                    "personnelles et de santé soient collectées, stockées et traitées " +
                                    "conformément à la loi sénégalaise n° 2008-12 du 25 janvier 2008 " +
                                    "sur la protection des données à caractère personnel.\n\n" +
                                    "Vos données sont utilisées exclusivement pour la gestion de votre " +
                                    "dossier médical électronique, la coordination de vos soins entre " +
                                    "les professionnels de santé autorisés, et l'envoi de notifications " +
                                    "liées à vos rendez-vous et résultats.\n\n" +
                                    "Vos données ne sont jamais vendues à des tiers. Vous disposez d'un " +
                                    "droit d'accès, de rectification et de suppression en contactant " +
                                    "mediconnect10@gmail.com."
                    )
                    .version("1.0")
                    .obligatoire(true)
                    .build());
            log.info("Template POLITIQUE_CONFIDENTIALITE créé.");
        }

        if (!consentementRepository.existsByTypeConsentement(
                TypeConsentement.DIAGNOSTIC_IA)) {
            consentementRepository.save(Consentement.builder()
                    .typeConsentement(TypeConsentement.DIAGNOSTIC_IA)
                    .titre("Utilisation de l'intelligence artificielle pour le diagnostic")
                    .contenu(
                            "En acceptant ce consentement, vous autorisez MediConnect à utiliser " +
                                    "vos données de santé (constantes, résultats d'examens, antécédents) " +
                                    "pour que le module d'IA propose des pistes diagnostiques à votre médecin.\n\n" +
                                    "Les propositions de l'IA sont des aides à la décision et ne remplacent " +
                                    "jamais le jugement clinique du médecin. Seule votre équipe soignante " +
                                    "accède aux résultats.\n\n" +
                                    "Vous pouvez retirer ce consentement à tout moment depuis votre espace " +
                                    "personnel ou en demandant à votre médecin ou infirmier."
                    )
                    .version("1.0")
                    .obligatoire(false)
                    .build());
            log.info("Template DIAGNOSTIC_IA créé.");
        }

        if (!consentementRepository.existsByTypeConsentement(
                TypeConsentement.TRAIN_MODELS)) {
            consentementRepository.save(Consentement.builder()
                    .typeConsentement(TypeConsentement.TRAIN_MODELS)
                    .titre("Participation à l'amélioration des modèles d'IA")
                    .contenu(
                            "En acceptant ce consentement, vous autorisez MediConnect à utiliser " +
                                    "vos données de santé, sous forme strictement anonymisée et agrégée, " +
                                    "pour améliorer les algorithmes d'IA de la plateforme.\n\n" +
                                    "Garanties : toute donnée transmise est au préalable dépouillée de " +
                                    "tout identifiant direct (nom, prénom, numéro de patient). " +
                                    "L'anonymisation est irréversible et conforme aux standards " +
                                    "internationaux (k-anonymat).\n\n" +
                                    "Vous pouvez retirer ce consentement à tout moment."
                    )
                    .version("1.0")
                    .obligatoire(false)
                    .build());
            log.info("Template TRAIN_MODELS créé.");
        }
    }

    private void importerOrdreMedecins() {
        if (ordreMedecinRepository.count() == 0) {
            log.info("Importation du tableau de l'Ordre des Médecins...");
            try {
                ordreMedecinImportService.importer();
                log.info("Importation terminée : {} médecins chargés",
                        ordreMedecinRepository.count());
            } catch (Exception e) {
                log.warn("Importation Ordre échouée (réseau?) : {}. "
                        + "La vérification manuelle sera désactivée.", e.getMessage());
            }
        } else {
            log.info("Tableau Ordre des Médecins déjà chargé ({} entrées)",
                    ordreMedecinRepository.count());
        }
    }
}