package sn.edu.ept.mediconnect.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.auth.entities.OtpCode;
import sn.edu.ept.mediconnect.auth.entities.PasswordResetToken;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;
import sn.edu.ept.mediconnect.users.medecin.MedecinRepository;
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
import sn.edu.ept.mediconnect.users.medecin.Cardiologue;
import sn.edu.ept.mediconnect.users.medecin.Medecin;
import sn.edu.ept.mediconnect.users.medecin.Specialite;
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
    private final OrdreMedecinRepository ordreRepo;
    private final OtpService              otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtService jwtService;
    private final PasswordEncoder         passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final PatientNumeroService patientNumeroService;

    // INSCRIPTION
    public RegisterResponse register(RegisterRequest req) {

        // email ou téléphone obligatoire
        if (estVide(req.getEmail()) && estVide(req.getTelephone())) {
            throw BusinessException.badRequest(
                "Vous devez fournir un email ou un numéro de téléphone " +
                "pour recevoir votre code de vérification.");
        }

        // vérifier unicité email si fourni
        if (!estVide(req.getEmail())
                && userRepo.existsByEmail(req.getEmail())) {
            throw BusinessException.conflict("Cet email est déjà utilisé.");
        }

        // pour MÉDECIN et CARDIOLOGUE : vérifier dans la table de l'Ordre de medecins
        if (req.getRole() == Role.MEDECIN || req.getRole() == Role.CARDIOLOGUE) {
            verifyOrdre(req);
        }

        // créer l'utilisateur selon le rôle (compte INACTIF par defaut)
        Long userId = createUser(req);

        // générer et envoyer l'OTP
        OtpCode otp = otpService.generateAndSend(
            userId,
            req.getEmail(),
            req.getTelephone(),
            TypeOtp.INSCRIPTION
        );

        String canalNom = otp.getCanal().name();
        String dest     = maskDestination(otp.getDestination());

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
                req.getNom().trim().toUpperCase()
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

            case PATIENT ->
                    createPatient(req);

            case INFIRMIER ->
                    createInfirm(req);

            default ->
                    throw BusinessException.badRequest(
                            "Rôle non supporté"
                    );
        };
    }

    private Long createMedecin(RegisterRequest req) {
        Medecin medecin = new Medecin();
        remplirChampCommuns(medecin, req);
        medecin.setNumOrdre(req.getNumOrdre().trim().toUpperCase());
        if (req.getSection() != null)
            medecin.setSection(req.getSection());
        medecin.setSpecialite(req.getSpecialite());
        medecin.setEtablissement(req.getEtablissement());
        medecin.setAdresse(req.getAdresse());
        medecin.setDisponible(false);
        medecin.setVerified(true); // Vérifié par l'Ordre
        medecinRepo.save(medecin);
        return medecin.getId();
    }

    private Long createCardio(RegisterRequest req) {
        Cardiologue cardiologue = new Cardiologue();
        remplirChampCommuns(cardiologue, req);
        cardiologue.setNumOrdre(req.getNumOrdre().trim().toUpperCase());
        if (req.getSection() != null)
            cardiologue.setSection(req.getSection());
        cardiologue.setSpecialite(Specialite.CARDIOLOGIE);
        cardiologue.setDisponible(false);
        cardiologue.setVerified(true);
        medecinRepo.save(cardiologue);
        return cardiologue.getId();
    }

    private Long createPatient(RegisterRequest req) {
        Patient patient = new Patient();
        remplirChampCommuns(patient, req);

        // Générer le numéro PAT-AAAAMMJJ
        patient.setNumPatient(patientNumeroService.generer());

        patient.setDateNaissance(req.getDateNaissance());

        if (req.getSexe() != null)
            patient.setSexe(req.getSexe());
        if (req.getGroupeSanguin() != null)
            patient.setGroupeSanguin(req.getGroupeSanguin());

        patient.setAssurance(req.getAssurance() != null ? req.getAssurance() : false);
        patientRepo.save(patient);
        return patient.getId();
    }

    // Dans createPatient, ajouter la résolution de l'infirmier
// via le token JWT de l'infirmier connecté
// ← à gérer dans un PatientController séparé (pas dans /auth/register)
    private Long createInfirm(RegisterRequest req) {
        Infirmier infirmier = new Infirmier();
        remplirChampCommuns(infirmier, req);
        infirmier.setServiceAffecte(req.getServiceAffecte());
        infirmier.setHopital(req.getHopital());
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
        User user = userRepo
                .findByEmailOrTelephone(
                        req.getEmail(),
                        req.getPhoneNumber()
                )
                .orElseThrow(() ->
                        BusinessException.badRequest(
                                "Identifiant ou mot de passe incorrect."
                        ));

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

    // RENVOYER OTP
    public RegisterResponse resendOtp(String email, String telephone) {

        User u = userRepo
                .findByEmailOrTelephone(
                        email,telephone
                )
                .orElseThrow(() ->
                        BusinessException.badRequest(
                                "Utilisateur introuvable."
                        ));

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

        User user = userRepo.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPrenom());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPrenom());
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Transactional
    public User updateUser(User user) {
        // Vérifier que l'utilisateur existe
        User existingUser = userRepo.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));


        // Mettre à jour uniquement les champs non nuls
        if (user.getPrenom() != null) {
            existingUser.setPrenom(user.getPrenom());
        }
        if (user.getNom() != null) {
            existingUser.setNom(user.getNom());
        }
        if (user.getTelephone() != null) {
            existingUser.setTelephone(user.getTelephone());
        }

        return userRepo.save(existingUser);
    }

//    public User getCurrentUser(Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new SecurityException("User not authenticated");
//        }
//
//        String username;
//
//        if (authentication.getPrincipal() instanceof UserDetails) {
//            username = ((UserDetails) authentication.getPrincipal()).getUsername();
//        } else {
//            username = authentication.getName();
//        }
//
//        // D'abord essayer de trouver par nom d'utilisateur
//        return userRepo.findByUsername(username)
//                .orElseGet(() -> {
//                    // Si non trouvé, essayer par email (pour la rétrocompatibilité)
//                    return userRepository.findByEmail(username)
//                            .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + username));
//                });
//    }


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