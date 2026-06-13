package sn.edu.ept.mediconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.common.entities.Role;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;
import sn.edu.ept.mediconnect.common.services.OrdreMedecinImportService;
import sn.edu.ept.mediconnect.consentement.Consentement;
import sn.edu.ept.mediconnect.consentement.ConsentementRepository;
import sn.edu.ept.mediconnect.consentement.TypeConsentement;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.UserRepository;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

import javax.sql.DataSource;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final OrdreMedecinImportService ordreMedecinImportService;
    private final OrdreMedecinRepository    ordreMedecinRepository;
    private final ConsentementRepository    consentementRepository;
    private final HopitalRepository         hopitalRepository;
    private final AdresseRepository         adresseRepository;
    private final DataSource                dataSource;
    private final UserRepository            userRepository;
    private final PatientRepository         patientRepository;
    private final PasswordEncoder           passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initialiserAdminParDefaut();
        initialiserPatientTest();
        initialiserTemplatesConsentements();
        initialiserHopitaux();
        initialiserAdresses();
        importerOrdreMedecins();
    }

    private void initialiserAdminParDefaut() {
        if (userRepository.existsByEmail("admin@mediconnect.sn")) {
            log.info("Compte admin déjà présent.");
            return;
        }
        User admin = new User();
        admin.setNom("MediConnect");
        admin.setPrenom("Admin");
        admin.setEmail("admin@mediconnect.sn");
        admin.setTelephone("+221000000000");
        admin.setMotDePasse(passwordEncoder.encode("Admin@2026"));
        admin.setRole(Role.ADMIN);
        admin.setActif(true);
        admin.setMfaActif(false);
        userRepository.save(admin);
        log.info("=== COMPTE ADMIN CRÉÉ ===");
        log.info("  Email    : admin@mediconnect.sn");
        log.info("  Password : Admin@2026");
        log.info("  → Changez ce mot de passe après la première connexion.");
        log.info("========================");
    }

    private void initialiserPatientTest() {
        if (patientRepository.existsByNumPatient("PAT-20260101-0001")) {
            log.info("Patient de test déjà présent.");
            return;
        }
        Patient patient = new Patient();
        patient.setNom("DIALLO");
        patient.setPrenom("Aminata");
        patient.setEmail("patient.test@mediconnect.sn");
        patient.setTelephone("+221771234567");
        patient.setMotDePasse(passwordEncoder.encode("Patient@2026"));
        patient.setRole(Role.PATIENT);
        patient.setActif(true);
        patient.setMfaActif(false);
        patient.setNumPatient("PAT-20260101-0001");
        patient.setDateNaissance(LocalDate.of(1990, 5, 15));
        patient.setAssurance(false);
        patient.setDemandeSuppressionEnAttente(false);
        patientRepository.save(patient);
        log.info("=== PATIENT TEST CRÉÉ ===");
        log.info("  Email      : patient.test@mediconnect.sn");
        log.info("  Téléphone  : +221771234567");
        log.info("  Password   : Patient@2026");
        log.info("  N° Patient : PAT-20260101-0001");
        log.info("  → Connectez-vous avec l'email OU le téléphone + le mot de passe.");
        log.info("=========================");
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

    private void initialiserHopitaux() {
        if (hopitalRepository.count() == 0) {
            log.info("Initialisation des hôpitaux...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("hopitaux-data.sql"));
                populator.setSqlScriptEncoding("UTF-8");
                populator.execute(dataSource);
                log.info("Hôpitaux chargés : {} entrées", hopitalRepository.count());
            } catch (Exception e) {
                log.warn("Erreur initialisation hôpitaux : {}", e.getMessage());
            }
        } else {
            log.info("Hôpitaux déjà chargés ({} entrées)", hopitalRepository.count());
        }
    }

    private void initialiserAdresses() {
        if (adresseRepository.count() == 0) {
            log.info("Initialisation des adresses...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("adresses-data.sql"));
                populator.setSqlScriptEncoding("UTF-8");
                populator.execute(dataSource);
                log.info("Adresses chargées : {} entrées", adresseRepository.count());
            } catch (Exception e) {
                log.warn("Erreur initialisation adresses : {}", e.getMessage());
            }
        } else {
            log.info("Adresses déjà chargées ({} entrées)", adresseRepository.count());
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