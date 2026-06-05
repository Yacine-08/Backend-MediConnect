package sn.edu.ept.mediconnect.auth.services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendOtp(String destinataire, String code, TypeOtp type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur, "MediConnect Sénégal");
            helper.setTo(destinataire);
            helper.setSubject(sujet(type));
            helper.setText(contenuHtml(code, type), true);

            mailSender.send(message);
            log.info("Email OTP envoyé à {}", destinataire);

        } catch (Exception e) {
            log.error("Erreur envoi email OTP à {} : {}", destinataire, e.getMessage());
        }
    }

    private String sujet(TypeOtp type) {
        return switch (type) {
            case INSCRIPTION     -> "MediConnect — Activation de votre compte";
            case RESET_PASSWORD  -> "MediConnect — Réinitialisation de mot de passe";
        };
    }

    private String contenuHtml(String code, TypeOtp type) {
        String action = type == TypeOtp.INSCRIPTION
            ? "activer votre compte" : "réinitialiser votre mot de passe";

        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: #1A3A6B; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">MediConnect Sénégal</h1>
              </div>
              <div style="padding: 30px; background: #f9f9f9;">
                <h2>Votre code de vérification</h2>
                <p>Pour %s, veuillez utiliser le code suivant :</p>
                <div style="background: #fff; border: 2px solid #1A3A6B;
                            padding: 20px; text-align: center; margin: 20px 0;
                            border-radius: 8px;">
                  <span style="font-size: 36px; font-weight: bold;
                               color: #1A3A6B; letter-spacing: 8px;">%s</span>
                </div>
                <p style="color: #666;">Ce code est valable pendant <strong>10 minutes</strong>.</p>
                <p style="color: #999; font-size: 12px;">
                  Si vous n'avez pas demandé ce code, ignorez cet email.
                </p>
              </div>
            </body>
            </html>
            """.formatted(action, code);
    }

    // send password reset email
    public void sendPasswordResetEmail(String to, String token, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur);
            helper.setTo(to);
            helper.setSubject("Réinitialisation de mot de passe - MediConnect");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetTemplate(firstName, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
    public static String buildPasswordResetTemplate(String prenom, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 8px; }
                        .button { display: inline-block; padding: 12px 30px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Réinitialisation de mot de passe</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
                            <div style="text-align: center;">
                                <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                            </div>
                            <p>Ce lien expirera dans <strong>24 heures</strong>.</p>
                            <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email ou contacter notre support.</p>
                            <p style="font-size: 12px; color: #666; margin-top: 20px;">Si le bouton ne fonctionne pas, copiez ce lien : %s</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 MediConnect Sénégal. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(prenom, resetUrl, resetUrl);
    }

    // confirm password changed
    public void sendPasswordChangedEmail(String to, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur);
            helper.setTo(to);
            helper.setSubject("Mot de passe modifié - MediConnect");

            String htmlContent = buildPasswordChangedTemplate(prenom);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
    public static String buildPasswordChangedTemplate(String prenom) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 8px; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Mot de passe modifié avec succès</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Votre mot de passe a été modifié avec succès.</p>
                            <p>Si vous n'êtes pas à l'origine de cette modification, veuillez contacter immédiatement notre support.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 MediConnect Sénégal. Tous droits réservés.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(prenom);
    }

    @Async
    public void sendMotDePasseTemporaire(String destinataire,
                                         String prenom,
                                         String nom,
                                         String numPatient,
                                         String motDePasseTemp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur, "MediConnect Sénégal");
            helper.setTo(destinataire);
            helper.setSubject("MediConnect — Bienvenue, votre compte patient est créé");
            helper.setText(buildMotDePasseTemporaireTemplate(
                    prenom, nom, numPatient, motDePasseTemp), true);

            mailSender.send(message);
            log.info("Email mot de passe temporaire envoyé à {}", destinataire);

        } catch (Exception e) {
            log.error("Erreur envoi email mot de passe temporaire à {} : {}",
                    destinataire, e.getMessage());
        }
    }

    private String buildMotDePasseTemporaireTemplate(String prenom,
                                                     String nom,
                                                     String numPatient,
                                                     String motDePasseTemp) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #1A3A6B; color: white;
                          padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background-color: #f9f9f9; padding: 30px; }
                .credentials { background: #ffffff; border: 2px solid #1A3A6B;
                               border-radius: 8px; padding: 20px; margin: 20px 0; }
                .label { font-size: 12px; color: #888; text-transform: uppercase;
                         font-weight: bold; margin-bottom: 4px; }
                .value { font-size: 18px; font-weight: bold; color: #1A3A6B;
                         letter-spacing: 2px; }
                .warning { background: #FFF3CD; border-left: 4px solid #FFC107;
                           padding: 12px 16px; margin-top: 20px;
                           border-radius: 0 4px 4px 0; font-size: 13px; }
                .footer { text-align: center; margin-top: 20px;
                          font-size: 12px; color: #888; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1 style="margin:0;">MediConnect Sénégal</h1>
                    <p style="margin:8px 0 0; opacity:0.85;">
                        Plateforme Nationale de Télémédecine
                    </p>
                </div>
                <div class="content">
                    <p>Bonjour <strong>%s %s</strong>,</p>
                    <p>
                        Votre dossier patient a été créé avec succès sur la plateforme
                        MediConnect Sénégal. Voici vos informations de connexion :
                    </p>

                    <div class="credentials">
                        <div class="label">Numéro patient</div>
                        <div class="value">%s</div>
                        <br>
                        <div class="label">Identifiant de connexion</div>
                        <div class="value" style="font-size:15px;">%s</div>
                        <br>
                        <div class="label">Mot de passe temporaire</div>
                        <div class="value" style="color:#C0392B;">%s</div>
                    </div>

                    <div class="warning">
                        <strong>Important :</strong> Ce mot de passe est temporaire.
                        Connectez-vous et changez-le dès votre première connexion
                        pour sécuriser votre compte.
                    </div>

                    <p style="margin-top:24px;">
                        En cas de problème, contactez votre établissement de santé
                        ou notre support.
                    </p>
                </div>
                <div class="footer">
                    <p>&copy; 2025 MediConnect Sénégal. Tous droits réservés.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(prenom, nom, numPatient,
                // Identifiant = email si dispo, sinon téléphone
                "Votre email ou numéro de téléphone enregistré",
                motDePasseTemp);
    }
}