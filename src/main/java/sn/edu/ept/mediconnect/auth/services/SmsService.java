package sn.edu.ept.mediconnect.auth.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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

    @Async
    public void sendOtp(String telephone, String code) {
        String message = "MediConnect - Votre code de verification : " + code
                       + " (valable 10 minutes)";
        try {
            if ("twilio".equals(provider)) {
                envoyerViaTwilio(telephone, message);
            } else if ("orange".equals(provider)) {
                envoyerViaOrange(telephone, message);
            } else {
                // Mode dev : log seulement
                log.info("SMS OTP [DEV] → {} : {}", telephone, message);
            }
        } catch (Exception e) {
            log.error("Erreur envoi SMS à {} : {}", telephone, e.getMessage());
        }
    }

    public void sendPasswordResetSms(String toPhoneNumber, String resetToken, String firstName) {
        try {
            Twilio.init(accountSid, authToken);
            String messageBody = String.format(
                    "Bonjour %s, voici votre lien de réinitialisation de mot de passe : http://votresite.com/reset-password?token=%s",
                    firstName,
                    resetToken
            );

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du SMS de réinitialisation: " + e.getMessage());
        }
    }

    private void envoyerViaTwilio(String telephone, String message) {
        // Intégration Twilio à implémenter
        log.info("Twilio SMS → {} : {}", telephone, message);
    }

    private void envoyerViaOrange(String telephone, String message) {
        // Intégration Orange SMS API Sénégal à implémenter
        log.info("Orange SMS → {} : {}", telephone, message);
    }
}