package sn.edu.ept.mediconnect.auth.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.provider:log}")
    private String provider;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @Value("${twilio.whatsapp.from:whatsapp:+14155238886}")
    private String whatsappFrom;

    @Value("${twilio.verify.service.sid:}")
    private String verifyServiceSid;

    /** Au démarrage, renomme le service Twilio Verify pour que le SMS affiche "Code OTP MediConnect". */
    @PostConstruct
    public void configurerNomServiceVerify() {
        if (!"twilio".equals(provider) || verifyServiceSid == null || verifyServiceSid.isBlank()) return;
        try {
            Twilio.init(accountSid, authToken);
            com.twilio.rest.verify.v2.Service.updater(verifyServiceSid)
                    .setFriendlyName("Code OTP MediConnect")
                    .update();
            log.info("Twilio Verify service renommé : Code OTP MediConnect");
        } catch (Exception e) {
            log.warn("Impossible de renommer le service Twilio Verify : {}", e.getMessage());
        }
    }

    @Async
    public void sendOtp(String telephone, String code) {
        String message = "MediConnect - Votre code de verification : " + code
                       + " (valable 10 minutes)";
        try {
            if ("twilio".equals(provider)) {
                envoyerViaVerify(telephone); // Twilio Verify génère et envoie son propre code
            } else if ("whatsapp".equals(provider)) {
                envoyerViaWhatsApp(telephone, message);
            } else if ("orange".equals(provider)) {
                envoyerViaOrange(telephone, message);
            } else {
                log.info("SMS OTP [DEV] → {} : {}", telephone, code);
            }
        } catch (Exception e) {
            log.error("Erreur envoi OTP à {} : {}", telephone, e.getMessage());
        }
    }

    /** Vérifie le code saisi via Twilio Verify. Retourne true si approuvé. */
    public boolean verifierViaVerify(String telephone, String code) {
        try {
            Twilio.init(accountSid, authToken);
            String e164 = toE164(telephone);
            VerificationCheck check = VerificationCheck.creator(verifyServiceSid)
                    .setTo(e164)
                    .setCode(code)
                    .create();
            boolean approved = "approved".equalsIgnoreCase(check.getStatus().toString());
            log.info("Twilio Verify check pour {} : {}", e164, check.getStatus());
            return approved;
        } catch (Exception e) {
            log.error("Erreur Twilio Verify check pour {} : {}", telephone, e.getMessage());
            return false;
        }
    }

    public void sendPasswordResetSms(String toPhoneNumber, String resetToken, String firstName) {
        try {
            Twilio.init(accountSid, authToken);
            String messageBody = String.format(
                    "Bonjour %s, voici votre lien de réinitialisation de mot de passe : " +
                    "http://votresite.com/reset-password?token=%s",
                    firstName, resetToken
            );
            Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du SMS de réinitialisation: " + e.getMessage());
        }
    }

    private void envoyerViaVerify(String telephone) {
        Twilio.init(accountSid, authToken);
        String e164 = toE164(telephone);
        Verification.creator(verifyServiceSid, e164, "sms").create();
        log.info("Twilio Verify OTP envoyé à {}", e164);
    }

    private void envoyerViaWhatsApp(String telephone, String message) {
        Twilio.init(accountSid, authToken);
        String e164 = toE164(telephone);
        Message.creator(
                new PhoneNumber("whatsapp:" + e164),
                new PhoneNumber(whatsappFrom),
                message
        ).create();
        log.info("WhatsApp OTP envoyé à {}", e164);
    }

    private void envoyerViaOrange(String telephone, String message) {
        // Intégration Orange SMS API Sénégal à implémenter
        log.info("Orange SMS → {} : {}", telephone, message);
    }

    // Convertit tout format sénégalais vers E.164 (+221XXXXXXXXX) sans espaces
    private String toE164(String telephone) {
        if (telephone == null) return null;
        String digits = telephone.replaceAll("[^0-9]", "");
        if (digits.startsWith("00221"))      digits = digits.substring(5);
        else if (digits.startsWith("221"))   digits = digits.substring(3);
        else if (digits.length() == 10 && digits.startsWith("0")) digits = digits.substring(1);
        if (digits.length() == 9) return "+221" + digits;
        return telephone.startsWith("+") ? telephone.replaceAll("\\s+", "") : "+" + digits;
    }
}
