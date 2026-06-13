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
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository        patientRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final UserRepository           userRepository;
    private final AdresseRepository        adresseRepository;
    private final PatientNumeroService     patientNumeroService;
    private final ConsentementService      consentementService;
    private final PasswordEncoder          passwordEncoder;
    private final EmailService             emailService;

    private static final String CHARS  = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // CRÉATION PAR L'ASSISTANT
    @Transactional
    public CreatePatientResponse create(Long assistantId, CreatePatientRequest req) {

        // L'assistant doit exister et être actif
        User assistant = userRepository.findById(assistantId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Assistant introuvable (id=" + assistantId + ")"));

        if (!Boolean.TRUE.equals(assistant.getActif())) {
            throw BusinessException.forbidden(
                    "Votre compte est inactif. Contactez l'administrateur.");
        }

        if (assistant.getRole() != Role.ASSISTANT) {
            throw BusinessException.forbidden(
                    "Seul un assistant peut créer un compte patient.");
        }

        String email     = normalizeEmail(req.getEmail());
        String telephone = req.getTelephone();

        if (estVide(email) && estVide(telephone)) {
            throw BusinessException.badRequest(
                    "Vous devez fournir un email ou un numéro de téléphone pour le patient.");
        }

        if (!estVide(email) && userRepository.existsByEmail(email)) {
            throw BusinessException.conflict("Cet email est déjà utilisé.");
        }

        if (!estVide(telephone) && userRepository.existsByTelephone(telephone)) {
            throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé.");
        }

        String motDePasseTemp = generateTempPassword();

        Patient patient = new Patient();
        patient.setNom(req.getNom().trim().toUpperCase());
        patient.setPrenom(req.getPrenom().trim());
        patient.setEmail(email);
        patient.setTelephone(telephone);
        patient.setMotDePasse(passwordEncoder.encode(motDePasseTemp));
        patient.setRole(Role.PATIENT);
        patient.setActif(true);
        patient.setMfaActif(false);

        patient.setNumPatient(patientNumeroService.generer());
        patient.setDateNaissance(req.getDateNaissance());
        patient.setSexe(req.getSexe());
        patient.setGroupeSanguin(req.getGroupeSanguin());
        patient.setAssurance(req.getAssurance() != null ? req.getAssurance() : false);
        patient.setCreePar(assistant);

        if (req.getAdresse() != null) {
            Adresse adresse = adresseRepository
                    .findByRegionAndDepartementAndCommune(
                            req.getAdresse().getRegion(),
                            req.getAdresse().getDepartement(),
                            req.getAdresse().getCommune())
                    .orElseGet(() -> adresseRepository.save(Adresse.builder()
                            .region(req.getAdresse().getRegion())
                            .departement(req.getAdresse().getDepartement())
                            .commune(req.getAdresse().getCommune())
                            .build()));
            patient.setAdresse(adresse);
        }

        patientRepository.save(patient);
        log.info("Patient créé par l'assistant {} : {} {} (numPatient={})",
                assistantId, patient.getPrenom(), patient.getNom(), patient.getNumPatient());

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
                assistant
        );

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

    // Patients créés par un assistant donné
    @Transactional(readOnly = true)
    public List<PatientResponse> getByCreateur(Long createurId) {
        return patientRepository.findByCreeParId(createurId)
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

    // Patients filtrés par spécialité du médecin (via consultations)
    @Transactional(readOnly = true)
    public List<PatientResponse> getBySpecialite(String specialite) {
        if (specialite == null || specialite.isBlank()) return getAll();
        return patientRepository.findByMedecinSpecialite(specialite)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> searchBySpecialite(String terme, String specialite) {
        if (specialite == null || specialite.isBlank()) return search(terme);
        return patientRepository.searchByMedecinSpecialite(terme, specialite)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest req) {

        Patient patient = find(id);

        String nouvelEmail      = normalizeEmail(req.getEmail());
        String nouveauTelephone = req.getTelephone();

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
            Adresse adresse = adresseRepository
                    .findByRegionAndDepartementAndCommune(
                            req.getAdresse().getRegion(),
                            req.getAdresse().getDepartement(),
                            req.getAdresse().getCommune())
                    .orElseGet(() -> adresseRepository.save(Adresse.builder()
                            .region(req.getAdresse().getRegion())
                            .departement(req.getAdresse().getDepartement())
                            .commune(req.getAdresse().getCommune())
                            .build()));
            patient.setAdresse(adresse);
        }

        patientRepository.save(patient);
        log.info("Patient mis à jour : id={}", id);
        return toResponse(patient);
    }

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

    @Transactional
    public void demanderSuppression(Long patientId) {
        Patient patient = find(patientId);
        patient.setDemandeSuppressionEnAttente(true);
        patientRepository.save(patient);
        log.info("Demande de suppression enregistrée : patient id={}", patientId);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> exportDonnees(Long patientId) {
        Patient patient = find(patientId);
        java.util.Map<String, Object> donnees = new java.util.LinkedHashMap<>();
        donnees.put("profil", toResponse(patient));
        donnees.put("exporteLe", java.time.LocalDateTime.now().toString());
        donnees.put("notice", "Données exportées conformément à la loi n°2008-12 sur la protection des données personnelles au Sénégal.");
        return donnees;
    }

    private Patient find(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Patient introuvable (id=" + id + ")"));
    }

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
            b.creeParId(p.getCreePar().getId())
             .creeParNomComplet(p.getCreePar().getPrenom() + " " + p.getCreePar().getNom());
        }
        if (p.getAdresse() != null) {
            b.region(p.getAdresse().getRegion())
             .departement(p.getAdresse().getDepartement())
             .commune(p.getAdresse().getCommune());
        }

        return b.build();
    }
}
