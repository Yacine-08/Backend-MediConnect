package sn.edu.ept.mediconnect.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.auth.entities.OtpCode;
import sn.edu.ept.mediconnect.auth.entities.PasswordResetToken;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Hopital;
import sn.edu.ept.mediconnect.common.repositories.AdresseRepository;
import sn.edu.ept.mediconnect.common.repositories.HopitalRepository;
import sn.edu.ept.mediconnect.users.medecin.*;
import sn.edu.ept.mediconnect.auth.repositories.PasswordResetTokenRepository;
import sn.edu.ept.mediconnect.users.UserRepository;
import sn.edu.ept.mediconnect.common.entities.Role;
import sn.edu.ept.mediconnect.common.repositories.OrdreMedecinRepository;
import sn.edu.ept.mediconnect.dtos.*;
import sn.edu.ept.mediconnect.exceptions.BadRequestException;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.exceptions.ResourceNotFoundException;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.assistant.Assistant;
import sn.edu.ept.mediconnect.users.assistant.AssistantRepository;
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
// import sn.edu.ept.mediconnect.users.patient.Patient;
// import sn.edu.ept.mediconnect.users.patient.PatientNumeroService;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import sn.edu.ept.mediconnect.users.infirmier.InfirmierRepository;
// import sn.edu.ept.mediconnect.users.patient.PatientRepository;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final MedecinRepository medecinRepo;
//     private final PatientRepository       patientRepo;
    private final InfirmierRepository     infirmierRepo;
    private final AssistantRepository     assistantRepo;
    private final CardiologueRepository cardiologueRepo;
    private final HopitalRepository hopitalRepo;
    private final OrdreMedecinRepository ordreRepo;
    private final AdresseRepository adresseRepository;
    private final OtpService              otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtService jwtService;
    private final PasswordEncoder         passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
//     private final PatientNumeroService patientNumeroService;

    // INSCRIPTION
    public RegisterResponse register(RegisterRequest req) {

        String email = req.getEmail() != null
                ? req.getEmail().trim().toLowerCase()
                : null;

        String telephone = req.getTelephone();

        // email ou téléphone obligatoire
        if (estVide(email) && estVide(telephone)) {

            throw BusinessException.badRequest(
                    "Vous devez fournir un email ou un numéro de téléphone.");
        }

        // unicité email
        if (!estVide(email)
                && userRepo.existsByEmail(email)) {

            throw BusinessException.conflict(
                    "Cet email est déjà utilisé.");
        }

        // unicité téléphone
        if (!estVide(telephone)
                && userRepo.existsByTelephone(telephone)) {

            throw BusinessException.conflict(
                    "Ce numéro de téléphone est déjà utilisé.");
        }

        // Vérification ordre médecins
        if (req.getRole() == Role.MEDECIN
                || req.getRole() == Role.CARDIOLOGUE) {

            verifyOrdre(req);
        }

        // création utilisateur
        Long userId = createUser(req);

        // OTP
        OtpCode otp = otpService.generateAndSend(
                userId,
                email,
                telephone,
                TypeOtp.INSCRIPTION
        );

        String canalNom = otp.getCanal().name();
        String dest = maskDestination(otp.getDestination());

        return RegisterResponse.builder()
                .userId(userId)
                .message("Un code de vérification a été envoyé par "
                        + canalNom + " à " + dest)
                .createdAt(new Date())
                .otpEnvoye(true)
                .build();
    }

    // VÉRIFICATION ORDRE MÉDECINS

    private void verifyOrdre(RegisterRequest req) {

        if (estVide(req.getNumOrdre())) {
            throw BusinessException.badRequest(
                "Le numéro d'ordre MSAS est obligatoire pour les médecins.");
        }

        // Si la table n'a pas été peuplée (import réseau raté au démarrage),
        // on ne bloque pas l'inscription — un admin pourra vérifier manuellement.
        if (ordreRepo.count() == 0) {
            log.warn("Table ordre_medecins vide — vérification désactivée pour {}",
                req.getNumOrdre());
            return;
        }

        String numOrdre = req.getNumOrdre().trim().toUpperCase();

        // Recherche par numéro d'ordre uniquement
        var ordreOpt = ordreRepo.findByNumOrdre(numOrdre);

        if (ordreOpt.isEmpty()) {
            throw BusinessException.badRequest(
                "Numéro d'ordre introuvable dans le tableau de l'Ordre " +
                "National des Médecins du Sénégal. " +
                "Vérifiez votre numéro.");
        }

        var ordre = ordreOpt.get();
        boolean nomOk    = ordre.getNom().equalsIgnoreCase(req.getNom().trim());
        boolean prenomOk = ordre.getPrenom().equalsIgnoreCase(req.getPrenom().trim());

        if (!nomOk || !prenomOk) {
            throw BusinessException.badRequest(
                "Le nom ou prénom fourni ne correspond pas au numéro d'ordre " +
                numOrdre + " dans le tableau de l'Ordre. " +
                "Vérifiez vos informations.");
        }

        // Validation de l'email institutionnel (@medisen.sn)
        if (ordre.getMail() != null && !ordre.getMail().isBlank()) {
            String reqEmail = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
            if (!ordre.getMail().equalsIgnoreCase(reqEmail)) {
                throw BusinessException.badRequest(
                    "Les informations fournies ne correspondent pas aux données de l'Ordre des Médecins. " +
                    "Vérifiez votre adresse email institutionnelle ou contactez l'administration.");
            }
        }

        log.info("Médecin vérifié : {} - {}", numOrdre, req.getNom());
    }


    // CRÉATION UTILISATEUR PAR RÔLE
    private Long createUser(RegisterRequest req) {

        return switch (req.getRole()) {

            case MEDECIN ->
                    createMedecin(req);

            case CARDIOLOGUE ->
                    createCardio(req);

            case INFIRMIER ->
                    createInfirm(req);

            case ASSISTANT ->
                    createAssistant(req);

            default ->
                    throw BusinessException.badRequest(
                            "Rôle non supporté"
                    );
        };
    }

    private Long createMedecin(RegisterRequest req) {

        // Si la spécialité est CARDIOLOGIE, on crée directement un Cardiologue
        // (qui est un sous-type de Medecin et s'insère dans les 3 tables d'un coup)
        if ("CARDIOLOGIE".equalsIgnoreCase(req.getSpecialite())) {
            return createCardio(req);
        }

        Medecin medecin = new Medecin();
        remplirChampCommuns(medecin, req);

        String numOrdreNorm = req.getNumOrdre().trim().toUpperCase();
        medecin.setNumOrdre(numOrdreNorm);

        // Spécialité et section proviennent de l'Ordre des Médecins (source de vérité)
        var ordreOpt = ordreRepo.findByNumOrdre(numOrdreNorm);
        medecin.setSection(ordreOpt.map(o -> o.getSection())
                .filter(s -> s != null && !s.isBlank())
                .orElse(req.getSection()));
        medecin.setSpecialite(ordreOpt.map(o -> o.getSpecialite())
                .filter(s -> s != null && !s.isBlank())
                .orElse(req.getSpecialite()));

        Hopital hopital = hopitalRepo.findById(req.getEtablissement().trim())
                .orElseThrow(() -> BusinessException.badRequest("Hôpital introuvable. Vérifiez votre sélection."));
        medecin.setEtablissement(hopital);

        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseGet(() -> adresseRepository.save(Adresse.builder()
                        .region(req.getAdresse().getRegion())
                        .departement(req.getAdresse().getDepartement())
                        .commune(req.getAdresse().getCommune())
                        .build()));

        medecin.setAdresse(adresse);
        medecin.setDisponible(true);
        medecin.setVerified(false); // validation admin requise

        medecinRepo.save(medecin);

        return medecin.getId();
    }

    private Long createCardio(RegisterRequest req) {

        Cardiologue cardiologue = new Cardiologue();
        remplirChampCommuns(cardiologue, req);

        String numOrdreCardio = req.getNumOrdre().trim().toUpperCase();
        cardiologue.setNumOrdre(numOrdreCardio);

        var ordreCardio = ordreRepo.findByNumOrdre(numOrdreCardio);
        cardiologue.setSection(ordreCardio.map(o -> o.getSection())
                .filter(s -> s != null && !s.isBlank())
                .orElse(req.getSection()));
        cardiologue.setSpecialite(ordreCardio.map(o -> o.getSpecialite())
                .filter(s -> s != null && !s.isBlank())
                .orElse(req.getSpecialite()));
        cardiologue.setDisponible(true);
        cardiologue.setVerified(false); // validation admin requise

        Hopital hopital = hopitalRepo
                .findById(req.getEtablissement().trim())
                .orElseThrow(() -> BusinessException.badRequest("Hôpital introuvable. Vérifiez votre sélection."));

        cardiologue.setEtablissement(hopital);

        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseGet(() -> adresseRepository.save(Adresse.builder()
                        .region(req.getAdresse().getRegion())
                        .departement(req.getAdresse().getDepartement())
                        .commune(req.getAdresse().getCommune())
                        .build()));

        cardiologue.setAdresse(adresse);

        // IMPORTANT : utiliser le bon repo
        cardiologueRepo.save(cardiologue);

        return cardiologue.getId();
    }

    private Long createInfirm(RegisterRequest req) {
        Infirmier infirmier = new Infirmier();
        remplirChampCommuns(infirmier, req);
        infirmier.setServiceAffecte(req.getServiceAffecte());
        Hopital hopital = hopitalRepo.findById(req.getHopital().trim())
                .orElseThrow(() ->
                        BusinessException.badRequest("Hôpital introuvable. Vérifiez votre sélection.")
                );
        infirmier.setHopital(hopital);
        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseGet(() -> adresseRepository.save(Adresse.builder()
                        .region(req.getAdresse().getRegion())
                        .departement(req.getAdresse().getDepartement())
                        .commune(req.getAdresse().getCommune())
                        .build()));

        infirmier.setAdresse(adresse);
        log.info("Adresse reçue: {}", req.getAdresse());
        log.info("Region: {}, Departement: {}, Commune: {}",
                req.getAdresse().getRegion(),
                req.getAdresse().getDepartement(),
                req.getAdresse().getCommune());
        infirmierRepo.save(infirmier);
        return infirmier.getId();
    }

    private Long createAssistant(RegisterRequest req) {
        Assistant assistant = new Assistant();
        remplirChampCommuns(assistant, req);
        assistant.setServiceAffecte(req.getServiceAffecte());
        Hopital hopital = hopitalRepo.findById(req.getHopital().trim())
                .orElseThrow(() ->
                        BusinessException.badRequest("Hôpital introuvable. Vérifiez votre sélection.")
                );
        assistant.setHopital(hopital);
        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseGet(() -> adresseRepository.save(Adresse.builder()
                        .region(req.getAdresse().getRegion())
                        .departement(req.getAdresse().getDepartement())
                        .commune(req.getAdresse().getCommune())
                        .build()));
        assistant.setAdresse(adresse);
        assistantRepo.save(assistant);
        return assistant.getId();
    }


    private void remplirChampCommuns(User u, RegisterRequest req) {
        u.setNom(req.getNom().trim().toUpperCase());
        u.setPrenom(req.getPrenom().trim());
        u.setEmail(req.getEmail() != null
            ? req.getEmail().toLowerCase().trim() : null);
        u.setTelephone(req.getTelephone());
        u.setMotDePasse(passwordEncoder.encode(req.getMotDePasse()));
        u.setRole(req.getRole());
        // Compte INACTIF jusqu'à validation OTP
        u.setActif(false);
    }

    // VALIDATION OTP → ACTIVATION DU COMPTE
    public void validateOtp(VerifyOtpRequest req) {

        String email = req.getEmail();
        String telephone = req.getTelephone();

        // Vérifier qu'au moins un identifiant est fourni
        if ((email == null || email.isBlank())
                && (telephone == null || telephone.isBlank())) {

            throw BusinessException.badRequest(
                    "Email ou téléphone obligatoire");
        }


        // Vérification OTP
        otpService.verify(
                email,
                telephone,
                req.getCode(),
                TypeOtp.INSCRIPTION
        );
        // Recherche utilisateur
        User user;

        if (email != null && !email.isBlank()) {

            user = userRepo.findByEmail(email)
                    .orElseThrow(() ->
                            BusinessException.notFound(
                                    "Utilisateur introuvable"));

        } else {

            user = userRepo.findByTelephone(telephone)
                    .orElseThrow(() ->
                            BusinessException.notFound(
                                    "Utilisateur introuvable"));
        }

        // Activation compte
        user.setActif(true);

        userRepo.save(user);

        log.info("Compte activé : {} ({})",
                user.getEmail(),
                user.getRole());

    }

    // CONNEXION
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest req) {

        // Recherche utilisateur par email ou téléphone
        User user;

        if (req.getEmail() != null && !req.getEmail().isBlank()) {

            user = userRepo.findByEmail(
                    req.getEmail().trim().toLowerCase()
            ).orElseThrow(() ->
                    BusinessException.badRequest(
                            "Identifiant ou mot de passe incorrect."
                    ));

        } else if (req.getPhoneNumber() != null
                && !req.getPhoneNumber().isBlank()) {

            String phone =
                    PhoneNumberUtils.normalizePhoneNumber(
                            req.getPhoneNumber()
                    );

            user = userRepo.findByTelephone(phone)
                    .orElseThrow(() ->
                            BusinessException.badRequest(
                                    "Identifiant ou mot de passe incorrect."
                            ));

        } else {

            throw BusinessException.badRequest(
                    "Email ou téléphone requis"
            );
        }

        // Vérifier que le compte est actif
        if (!user.getActif()) {
            throw BusinessException.badRequest(
                "Votre compte n'est pas encore activé. " +
                "Veuillez vérifier votre email ou téléphone pour le code OTP.");
        }

        // Médecins/Cardiologues : validation administrative requise
        if ((user.getRole() == Role.MEDECIN || user.getRole() == Role.CARDIOLOGUE)
                && user instanceof Medecin medecin
                && !Boolean.TRUE.equals(medecin.getVerified())) {
            throw BusinessException.badRequest(
                "COMPTE_EN_ATTENTE_VALIDATION — Votre dossier est en cours d'examen par l'administration. " +
                "Vous serez notifié par email une fois votre profil vérifié.");
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(
                req.getPassword(), user.getMotDePasse())) {
            throw BusinessException.badRequest(
                "Identifiant ou mot de passe incorrect.");
        }

        // Générer le JWT
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        // UserDto userDto;

        return AuthenticationResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .user(mapToUserDto(user))
            .build();
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getId());
        dto.setPrenom(user.getPrenom());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setRole(user.getRole());

        return dto;
    }

    public void blacklistToken(String token) {
        jwtService.blacklistToken(token);
    }

    // RENVOYER OTP
    public RegisterResponse resendOtp(String email, String telephone) {

        User u;

        if (email != null && !email.isBlank()) {

            u = userRepo.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() ->
                            BusinessException.badRequest(
                                    "Utilisateur introuvable."
                            ));

        } else if (telephone != null && !telephone.isBlank()) {

            String phone =
                    PhoneNumberUtils.normalizePhoneNumber(
                            telephone
                    );

            u = userRepo.findByTelephone(phone)
                    .orElseThrow(() ->
                            BusinessException.badRequest(
                                    "Utilisateur introuvable."
                            ));

        } else {

            throw BusinessException.badRequest(
                    "Email ou téléphone requis"
            );
        }

        if (u.getActif()) {
            throw BusinessException.badRequest("Ce compte est déjà activé.");
        }

        OtpCode otp = otpService.resend(
                u.getId(),
                u.getEmail(),
                u.getTelephone(),
                TypeOtp.INSCRIPTION
        );

        return RegisterResponse.builder()
            .userId(u.getId())
            .message("Nouveau code envoyé via " + otp.getCanal().name())
            .createdAt(new Date())
            .otpEnvoye(true)
            .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Aucun compte avec cet email"));
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String phone = PhoneNumberUtils.normalizePhoneNumber(request.getPhoneNumber());
            user = userRepo.findByTelephone(phone)
                    .orElseThrow(() -> new ResourceNotFoundException("Aucun compte avec ce numéro de téléphone"));
        } else {
            throw new BadRequestException("Veuillez fournir un email ou un numéro de téléphone");
        }

        otpService.generateAndSend(user.getId(), user.getEmail(), user.getTelephone(), TypeOtp.RESET_PASSWORD);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String telephone = request.getTelephone();

        if ((email == null || email.isBlank()) && (telephone == null || telephone.isBlank())) {
            throw new BadRequestException("Email ou téléphone obligatoire");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Le mot de passe et la confirmation ne correspondent pas");
        }

        otpService.verify(email, telephone, request.getCode(), TypeOtp.RESET_PASSWORD);

        User user;
        if (email != null && !email.isBlank()) {
            user = userRepo.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        } else {
            user = userRepo.findByTelephone(telephone)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendPasswordChangedEmail(user.getEmail(), user.getPrenom());
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // 1. Vérifier ancien mot de passe
        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        // 2. Vérifier confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Le mot de passe et la confirmation ne correspondent pas");
        }

        // 3. Nouveau doit être différent de l'ancien
        if (passwordEncoder.matches(request.getNewPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // 4. Mettre à jour
        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPrenom());
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }


    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
    private String maskDestination(String valeur) {
        if (valeur == null) return "";
        if (valeur.contains("@")) {
            int at = valeur.indexOf('@');
            return valeur.substring(0, Math.min(3, at))
                + "***" + valeur.substring(at);
        }
        return valeur.substring(0, Math.min(3, valeur.length()))
            + "***" + valeur.substring(valeur.length() - 2);
    }
}