package sn.edu.ept.mediconnect.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.mediconnect.auth.entities.CanalOtp;
import sn.edu.ept.mediconnect.auth.entities.OtpCode;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;
import sn.edu.ept.mediconnect.auth.repositories.OtpCodeRepository;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpService {

    private static final int    OTP_LONGUEUR      = 6;
    private static final int    OTP_EXPIRY_MINUTES = 10;
    private static final int    MAX_TENTATIVES     = 5;

    private final OtpCodeRepository otpRepo;
    private final EmailService      emailService;
    private final SmsService        smsService;
    private final UserRepository userRepo;

    //Générer et envoyer l'OTP

    public OtpCode generateAndSend(Long utilisateurId,
                                    String email,
                                    String telephone,
                                    TypeOtp type) {

        // Invalider les anciens OTP du même type
        otpRepo.invalidateLastOtp(utilisateurId, type);

        // Générer le code à 6 chiffres
        String code = generateCode();

        // Choisir le canal : EMAIL prioritaire, sinon SMS
        CanalOtp canal;
        String destination;

        if (email != null && !email.isBlank()) {
            canal       = CanalOtp.EMAIL;
            destination = email;
        } else if (telephone != null && !telephone.isBlank()) {
            canal       = CanalOtp.SMS;
            destination = telephone;
        } else {
            throw BusinessException.badRequest(
                "Email ou numéro de téléphone requis pour recevoir le code OTP");
        }

        // Sauvegarder en base
        OtpCode otp = OtpCode.builder()
            .userId(utilisateurId)
            .code(code)
            .type(type)
            .canal(canal)
            .destination(destination)
            .expireA(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();

        otpRepo.save(otp);

        // Envoyer selon le canal
        if (canal == CanalOtp.EMAIL) {
            emailService.sendOtp(destination, code, type);
        } else {
            smsService.sendOtp(destination, code);
        }

        log.info("OTP envoyé via {} à {} pour utilisateur {}",
            canal, masquer(destination), utilisateurId);

        return otp;
    }

    // Vérifier l'OTP saisi
    public void verify(String email, String telephone, String codeSaisi, TypeOtp type) {

        // Vérifier qu'au moins un identifiant est fourni
        if ((email == null || email.isBlank())
                && (telephone == null || telephone.isBlank())) {

            throw BusinessException.badRequest(
                    "Email ou téléphone obligatoire");
        }
        // Rechercher utilisateur
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

        // Rechercher OTP valide
        OtpCode otp = otpRepo.findLastValidOtp(
                        user.getId(),
                        type,
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        BusinessException.badRequest(
                                "Code OTP expiré ou inexistant. "
                                        + "Veuillez en demander un nouveau."
                        ));

        // Incrémenter tentatives
        otp.incrementTentatives();

        otpRepo.save(otp);

        // Vérifier nombre tentatives
        if (otp.getTentatives() > MAX_TENTATIVES) {

            throw BusinessException.badRequest(
                    "Trop de tentatives. "
                            + "Veuillez demander un nouveau code OTP.");
        }

        // Vérifier code
        if (!otp.getCode().equals(codeSaisi)) {

            int restantes =
                    MAX_TENTATIVES - otp.getTentatives();

            throw BusinessException.badRequest(
                    "Code OTP incorrect. "
                            + restantes
                            + " tentative(s) restante(s)."
            );
        }

        // Marquer utilisé
        otp.used();

        otpRepo.save(otp);

        log.info(
                "OTP vérifié avec succès pour utilisateur {}",
                user.getId()
        );
    }

    // Renvoyer un OTP

    public OtpCode resend(  Long userId,
                             String email,
                             String telephone,
                             TypeOtp type) {
        return generateAndSend(userId,email, telephone, type);
    }

    //Nettoyage planifié (chaque nuit à 2h)
    @Scheduled(cron = "0 0 2 * * *")
    public void nettoyerOtpExpires() {
        otpRepo.deleteExpired(LocalDateTime.now());
        log.info("Nettoyage OTP expirés effectué");
    }

    // Utilitaires
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // Masque l'email ou le téléphone pour les logs
    private String masquer(String valeur) {
        if (valeur.contains("@")) {
            int at = valeur.indexOf('@');
            return valeur.substring(0, Math.min(3, at))
                + "***" + valeur.substring(at);
        }
        return valeur.substring(0, Math.min(3, valeur.length())) + "***";
    }
}