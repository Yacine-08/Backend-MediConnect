package sn.edu.ept.mediconnect.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import sn.edu.ept.mediconnect.auth.entities.CanalOtp;
import sn.edu.ept.mediconnect.auth.entities.OtpCode;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;
import sn.edu.ept.mediconnect.auth.repositories.OtpCodeRepository;
import sn.edu.ept.mediconnect.exceptions.BusinessException;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.users.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_TENTATIVES     = 5;

    @Value("${sms.provider:log}")
    private String smsProvider;

    private final OtpCodeRepository otpRepo;
    private final EmailService      emailService;
    private final SmsService        smsService;
    private final UserRepository    userRepo;

    //Générer et envoyer l'OTP

    public OtpCode generateAndSend(Long utilisateurId,
                                    String email,
                                    String telephone,
                                    TypeOtp type) {

        // Invalider les anciens OTP du même type
        otpRepo.invalidateLastOtp(utilisateurId, type);

        // Générer le code à 6 chiffres
        String code = generateCode();

        // Choisir le canal :
        // Si un fournisseur SMS réel (whatsapp/twilio/orange) est configuré et qu'un
        // numéro de téléphone est disponible → on envoie via SMS/WhatsApp.
        // Sinon l'email est prioritaire, le téléphone en dernier recours.
        CanalOtp canal;
        String destination;

        boolean smsActif = !"log".equals(smsProvider)
                && telephone != null && !telephone.isBlank();

        if (smsActif) {
            canal       = CanalOtp.SMS;
            destination = telephone;
        } else if (email != null && !email.isBlank()) {
            canal       = CanalOtp.EMAIL;
            destination = email;
        } else if (telephone != null && !telephone.isBlank()) {
            canal       = CanalOtp.SMS;
            destination = telephone;
        } else {
            throw BusinessException.badRequest(
                "Email ou numéro de téléphone requis pour recevoir le code OTP");
        }

        // Pour Twilio Verify, c'est Twilio qui génère et envoie son propre code.
        // On stocke un marqueur en base ; la vérification se fera via l'API Twilio.
        String codeStocke = "twilio".equals(smsProvider) && canal == CanalOtp.SMS
                ? "TWILIO"
                : code;

        // Sauvegarder en base
        OtpCode otp = OtpCode.builder()
            .userId(utilisateurId)
            .code(codeStocke)
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

        // ── Branche Twilio Verify : Twilio gère son propre code et sa propre expiration ──
        // On bypass la DB car c'est Twilio qui est source de vérité.
        String userTelephone = user.getTelephone();
        boolean isTwilioVerify = "twilio".equals(smsProvider)
                && userTelephone != null && !userTelephone.isBlank();

        if (isTwilioVerify) {
            boolean ok = smsService.verifierViaVerify(userTelephone, codeSaisi);
            if (!ok) {
                throw BusinessException.badRequest("Code OTP incorrect ou expiré.");
            }
            // Invalider les records DB éventuels pour ce user/type
            otpRepo.invalidateLastOtp(user.getId(), type);
            log.info("OTP vérifié via Twilio Verify pour utilisateur {}", user.getId());
            return;
        }

        // ── Branche standard (email / log) : vérification via la DB ──
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