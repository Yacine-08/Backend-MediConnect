package sn.edu.ept.mediconnect.users.patient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.auth.services.EmailService;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Role;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.consentement.ConsentementService;
import sn.edu.ept.mediconnect.dtos.CreatePatientRequest;
import sn.edu.ept.mediconnect.dtos.CreatePatientResponse;
import sn.edu.ept.mediconnect.dtos.PatientResponse;
import sn.edu.ept.mediconnect.dtos.UpdatePatientRequest;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.medical.dossier.DossierMedical;
import sn.edu.ept.mediconnect.medical.dossier.DossierMedicalRepository;
import sn.edu.ept.mediconnect.medical.dossier.StatutDossier;
import sn.edu.ept.mediconnect.users.UserRepository;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
import sn.edu.ept.mediconnect.users.infirmier.InfirmierRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository   patientRepository;
    private final InfirmierRepository infirmierRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final UserRepository      userRepository;
    private final AdresseRepository   adresseRepository;
    private final PatientNumeroService patientNumeroService;
    private final ConsentementService consentementService;
    private final PasswordEncoder     passwordEncoder;
    private final EmailService emailService;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    //  CRÉATION PAR L'INFIRMIER
    @Transactional
    public CreatePatientResponse create(Long infirmierId, CreatePatientRequest req) {

        // Vérifier que l'infirmier existe et est actif
        Infirmier infirmier = infirmierRepository.findById(infirmierId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Infirmier introuvable (id=" + infirmierId + ")"));

        if (!Boolean.TRUE.equals(infirmier.getActif())) {
            throw BusinessException.forbidden(
                    "Votre compte est inactif. Contactez l'administrateur.");
        }

        // Au moins email ou téléphone requis
        String email     = normalizeEmail(req.getEmail());
        String telephone = req.getTelephone();

        if (estVide(email) && estVide(telephone)) {
            throw BusinessException.badRequest(
                    "Vous devez fournir un email ou un numéro de téléphone pour le patient.");
        }

        // Unicité email
        if (!estVide(email) && userRepository.existsByEmail(email)) {
            throw BusinessException.conflict("Cet email est déjà utilisé.");
        }

        // Unicité téléphone
        if (!estVide(telephone) && userRepository.existsByTelephone(telephone)) {
            throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé.");
        }

        // Générer mot de passe temporaire
        String motDePasseTemp = generateTempPassword();

        // Construire l'entité Patient
        Patient patient = new Patient();
        patient.setNom(req.getNom().trim().toUpperCase());
        patient.setPrenom(req.getPrenom().trim());
        patient.setEmail(email);
        patient.setTelephone(telephone);
        patient.setMotDePasse(passwordEncoder.encode(motDePasseTemp));
        patient.setRole(Role.PATIENT);
        // Compte actif immédiatement : c'est l'infirmier qui valide l'identité
        patient.setActif(true);
        patient.setMfaActif(false);

        patient.setNumPatient(patientNumeroService.generer());
        patient.setDateNaissance(req.getDateNaissance());
        patient.setSexe(req.getSexe());
        patient.setGroupeSanguin(req.getGroupeSanguin());
        patient.setAssurance(req.getAssurance() != null ? req.getAssurance() : false);
        patient.setCreePar(infirmier);

        if (req.getAdresse() != null) {
            Adresse adresse = adresseRepository.findByRegionAndDepartementAndCommune(
                            req.getAdresse().getRegion(),
                            req.getAdresse().getDepartement(),
                            req.getAdresse().getCommune())
                    .orElseThrow(() -> BusinessException.badRequest(
                            "Adresse introuvable : "
                            + req.getAdresse().getRegion() + " / "
                            + req.getAdresse().getDepartement() + " / "
                            + req.getAdresse().getCommune()));
            patient.setAdresse(adresse);
        }

        patientRepository.save(patient);
        log.info("Patient créé par l'infirmier {} : {} {} (numPatient={})",
                infirmierId, patient.getPrenom(), patient.getNom(), patient.getNumPatient());

        // Creation automatique du DME
        DossierMedical dme = DossierMedical.builder()
                .patient(patient)
                .statut(StatutDossier.ACTIF)
                .build();
        dossierMedicalRepository.save(dme);
        log.info("Dossier médical créé automatiquement — patient id={} numPatient={}",
                patient.getId(), patient.getNumPatient());

        consentementService.saveConsentement(
                patient,
                Boolean.TRUE.equals(req.getAcceptePolitiqueConfidentialite()),
                infirmier
        );

        // envoyer uniquement si le patient a un email
        if (!estVide(patient.getEmail())) {
            emailService.sendMotDePasseTemporaire(
                    patient.getEmail(),
                    patient.getPrenom(),
                    patient.getNom(),
                    patient.getNumPatient(),
                    motDePasseTemp
            );
        }

        return CreatePatientResponse.builder()
                .patientId(patient.getId())
                .numPatient(patient.getNumPatient())
                .nomComplet(patient.getPrenom() + " " + patient.getNom())
                .email(patient.getEmail())
                .telephone(patient.getTelephone())
                .motDePasseTemporaire(motDePasseTemp)
                .message("Compte patient et dossier médical créés avec succès. "
                       + "Communiquez le mot de passe temporaire au patient.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAll() {
        return patientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getByInfirmier(Long infirmierId) {
        return patientRepository.findByCreeParId(infirmierId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> search(String terme) {
        return patientRepository.search(terme)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    //  DÉTAIL
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return toResponse(find(id));
    }


    //  MISE À JOUR
    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest req) {

        Patient patient = find(id);

        String nouvelEmail     = normalizeEmail(req.getEmail());
        String nouveauTelephone = req.getTelephone();

        // Vérifier unicité uniquement si la valeur change
        if (!estVide(nouvelEmail) && !nouvelEmail.equals(patient.getEmail())) {
            if (userRepository.existsByEmail(nouvelEmail)) {
                throw BusinessException.conflict("Cet email est déjà utilisé.");
            }
            patient.setEmail(nouvelEmail);
        }

        if (!estVide(nouveauTelephone) && !nouveauTelephone.equals(patient.getTelephone())) {
            if (userRepository.existsByTelephone(nouveauTelephone)) {
                throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé.");
            }
            patient.setTelephone(nouveauTelephone);
        }

        if (!estVide(req.getNom()))    patient.setNom(req.getNom().trim().toUpperCase());
        if (!estVide(req.getPrenom())) patient.setPrenom(req.getPrenom().trim());
        if (req.getDateNaissance() != null) patient.setDateNaissance(req.getDateNaissance());
        if (req.getSexe() != null)          patient.setSexe(req.getSexe());
        if (req.getGroupeSanguin() != null) patient.setGroupeSanguin(req.getGroupeSanguin());
        if (req.getAssurance() != null)     patient.setAssurance(req.getAssurance());

        if (req.getAdresse() != null) {
            Adresse adresse = adresseRepository.findByRegionAndDepartementAndCommune(
                            req.getAdresse().getRegion(),
                            req.getAdresse().getDepartement(),
                            req.getAdresse().getCommune())
                    .orElseThrow(() -> BusinessException.badRequest("Adresse introuvable."));
            patient.setAdresse(adresse);
        }

        patientRepository.save(patient);
        log.info("Patient mis à jour : id={}", id);
        return toResponse(patient);
    }
    
    //  ACTIVATION / DÉSACTIVATION
    @Transactional
    public PatientResponse activate(Long id) {
        Patient patient = find(id);
        patient.setActif(true);
        patientRepository.save(patient);
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse desactivate(Long id) {
        Patient patient = find(id);
        patient.setActif(false);
        patientRepository.save(patient);
        return toResponse(patient);
    }
    
    
    //  HELPERS PRIVÉS
    private Patient find(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + id + ")"));
    }

    /**
     * Génère un mot de passe temporaire de 10 caractères :
     * format  Med@XXXXXXXX  pour être mémorisable tout en respectant
     * les critères de complexité (majuscule, chiffre, caractère spécial).
     */
    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder("Med@");
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private boolean estVide(String s) {
        return s == null || s.isBlank();
    }

    private String normalizeEmail(String email) {
        return (email != null && !email.isBlank())
                ? email.trim().toLowerCase()
                : null;
    }

    public PatientResponse toResponse(Patient p) {
        PatientResponse.PatientResponseBuilder b = PatientResponse.builder()
                .id(p.getId())
                .numPatient(p.getNumPatient())
                .nom(p.getNom())
                .prenom(p.getPrenom())
                .email(p.getEmail())
                .telephone(p.getTelephone())
                .dateNaissance(p.getDateNaissance())
                .sexe(p.getSexe())
                .groupeSanguin(p.getGroupeSanguin())
                .assurance(p.getAssurance())
                .actif(p.getActif())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt());

        if (p.getCreePar() != null) {
            b.infirmierId(p.getCreePar().getId())
             .infirmierNomComplet(p.getCreePar().getPrenom() + " " + p.getCreePar().getNom());
        }
        if (p.getAdresse() != null) {
            b.region(p.getAdresse().getRegion())
             .departement(p.getAdresse().getDepartement())
             .commune(p.getAdresse().getCommune());
        }

        return b.build();
    }
}