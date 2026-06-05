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
import sn.edu.ept.mediconnect.users.infirmier.Infirmier;
import sn.edu.ept.mediconnect.users.patient.Patient;
import sn.edu.ept.mediconnect.users.patient.PatientNumeroService;
import sn.edu.ept.mediconnect.utils.PhoneNumberUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import sn.edu.ept.mediconnect.users.infirmier.InfirmierRepository;
import sn.edu.ept.mediconnect.users.patient.PatientRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final MedecinRepository medecinRepo;
    private final PatientRepository       patientRepo;
    private final InfirmierRepository     infirmierRepo;
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
    private final PatientNumeroService patientNumeroService;

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

        // Vérification par numéro + nom dans le tableau de l'Ordre
        boolean trouve = ordreRepo.verifier(
                req.getNumOrdre().trim().toUpperCase(),
                req.getNom().trim().toUpperCase(),
                req.getPrenom().trim().toUpperCase(),
                req.getSection(),
                req.getSpecialite()
            ).isPresent();

        if (!trouve) {
            // Essai avec le numéro seul (au cas où le nom diffère légèrement)
            boolean existeNumero = ordreRepo.existsByNumOrdre(
                req.getNumOrdre().trim().toUpperCase());

            if (!existeNumero) {
                throw BusinessException.badRequest(
                    "Numéro d'ordre introuvable dans le tableau de l'Ordre " +
                    "National des Médecins du Sénégal. " +
                    "Vérifiez votre numéro ");
            } else {
                throw BusinessException.badRequest(
                    "Le nom fourni ne correspond pas au numéro d'ordre " +
                    req.getNumOrdre() + " dans le tableau de l'Ordre. " +
                    "Vérifiez vos informations.");
            }
        }

        log.info("Médecin vérifié : {} - {}",
            req.getNumOrdre(), req.getNom());
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

        medecin.setNumOrdre(req.getNumOrdre().trim().toUpperCase());
        medecin.setSection(req.getSection());
        medecin.setSpecialite(req.getSpecialite());

        Hopital hopital = hopitalRepo.findByNom(req.getEtablissement())
                .orElseThrow(() -> BusinessException.badRequest("Hôpital introuvable"));
        medecin.setEtablissement(hopital);

        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseThrow(() -> BusinessException.badRequest("Adresse introuvable"));

        medecin.setAdresse(adresse);
        medecin.setDisponible(false);
        medecin.setVerified(true);

        medecinRepo.save(medecin);

        return medecin.getId();
    }

    private Long createCardio(RegisterRequest req) {

        Cardiologue cardiologue = new Cardiologue();
        remplirChampCommuns(cardiologue, req);

        cardiologue.setNumOrdre(req.getNumOrdre().trim().toUpperCase());

        if (req.getSection() != null)
            cardiologue.setSection(req.getSection());

        cardiologue.setSpecialite(req.getSpecialite());
        cardiologue.setDisponible(false);
        cardiologue.setVerified(true);

        Hopital hopital = hopitalRepo
                .findByNom(req.getEtablissement())
                .orElseThrow(() -> BusinessException.badRequest("Hôpital introuvable"));

        cardiologue.setEtablissement(hopital);

        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseThrow(() -> BusinessException.badRequest("Adresse introuvable"));

        cardiologue.setAdresse(adresse);

        // IMPORTANT : utiliser le bon repo
        cardiologueRepo.save(cardiologue);

        return cardiologue.getId();
    }

    private Long createInfirm(RegisterRequest req) {
        Infirmier infirmier = new Infirmier();
        remplirChampCommuns(infirmier, req);
        infirmier.setServiceAffecte(req.getServiceAffecte());
        Hopital hopital = hopitalRepo.findByNom(req.getHopital())
                .orElseThrow(() ->
                        BusinessException.badRequest("Hôpital introuvable")
                );
        infirmier.setHopital(hopital);
        Adresse adresse = adresseRepository
                .findByRegionAndDepartementAndCommune(
                        req.getAdresse().getRegion(),
                        req.getAdresse().getDepartement(),
                        req.getAdresse().getCommune()
                )
                .orElseThrow(() -> new RuntimeException("Adresse introuvable"));

        infirmier.setAdresse(adresse);
        log.info("Adresse reçue: {}", req.getAdresse());
        log.info("Region: {}, Departement: {}, Commune: {}",
                req.getAdresse().getRegion(),
                req.getAdresse().getDepartement(),
                req.getAdresse().getCommune());
        infirmierRepo.save(infirmier);
        return infirmier.getId();
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

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(
                req.getPassword(), user.getMotDePasse())) {
            throw BusinessException.badRequest(
                "Identifiant ou mot de passe incorrect.");
        }

        // Générer le JWT
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        UserDto userDto;

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

        // delete old tokens
        passwordResetTokenRepo.deleteByUserId(user.getId());

        // create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        passwordResetTokenRepo.save(resetToken);

        // send email or SMS based on user's preferred contact method
        String prenom = (user.getPrenom() != null && !user.getPrenom().isBlank()) ?
                user.getPrenom() : "User";
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendPasswordResetEmail(user.getEmail(), token, prenom);
        } else if (user.getTelephone() != null && !user.getTelephone().isBlank()) {
            smsService.sendPasswordResetSms(user.getTelephone(), token, prenom);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token invalide"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Ce token a déjà été utilisé");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Ce token a expiré");
        }

        // Vérifier la confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "Le mot de passe et la confirmation ne correspondent pas");
        }

        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Utilisateur non trouvé"));

        // Optionnel : empêcher de réutiliser le mot de passe actuel
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getMotDePasse())) {

            throw new BadRequestException(
                    "Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setMotDePasse(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepo.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);

        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getPrenom()
        );
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