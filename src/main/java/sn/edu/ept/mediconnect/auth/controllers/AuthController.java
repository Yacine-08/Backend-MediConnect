package sn.edu.ept.mediconnect.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.edu.ept.mediconnect.auth.services.AuthService;
import sn.edu.ept.mediconnect.users.User;
import sn.edu.ept.mediconnect.dtos.*;
import sn.edu.ept.mediconnect.dtos.ResetPasswordRequest;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    @Operation(summary = "Inscription d'un utilisateur", description = "Crée un compte utilisateur et envoie un code OTP pour activer le compte.")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        try {
            RegisterResponse response = authService.register(req);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Inscription réussie. Veuillez vérifier votre email pour activer votre compte.",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // POST /api/auth/verify-otp
    @PostMapping("/verify-otp")
    @Operation(summary = "Vérification du code OTP", description = "Vérifie le code OTP envoyé par email pour activer le compte.")
    public ResponseEntity<?> verifierOtp(
            @Valid @RequestBody VerifyOtpRequest req) {

        try {
            authService.validateOtp(req);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Code OTP vérifié avec succès. Votre compte est maintenant actif."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // POST /api/auth/resend-otp
    @PostMapping("/resend-otp")
    @Operation(summary = "Renvoyer le code OTP", description = "Génère et renvoie un nouveau code OTP par email ou SMS.")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                    (request.getTelephone() == null || request.getTelephone().isBlank())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Veuillez fournir un email ou un numéro de téléphone"
                ));
            }

            authService.resendOtp(request.getEmail(), request.getTelephone());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Un nouveau code OTP a été envoyé"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur et retourne un jeton JWT.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Connexion réussie",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Commence la procédure de réinitialisation du mot de passe.")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Un email de réinitialisation a été envoyé à votre adresse"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Réinitialise le mot de passe à l'aide du token de réinitialisation.")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mot de passe réinitialisé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Changer le mot de passe", description = "Modifie le mot de passe de l'utilisateur connecté.")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal sn.edu.ept.mediconnect.users.User userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            // Extraire l'ID utilisateur depuis UserDetails
            Long userId = userDetails.getId();
            authService.changePassword(userId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mot de passe modifié avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/current-user")
    @Operation(
            summary = "Informations de l'utilisateur connecté",
            description = "Retourne les informations du profil de l'utilisateur actuellement connecté."
    )
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal sn.edu.ept.mediconnect.users.User userDetails
    ) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Utilisateur non authentifié"
            ));
        }

        UserDto userDto = new UserDto();
        userDto.setUserId(userDetails.getId());
        userDto.setPrenom(userDetails.getPrenom());
        userDto.setNom(userDetails.getNom());
        userDto.setEmail(userDetails.getEmail());
        userDto.setTelephone(userDetails.getTelephone());
        userDto.setRole(userDetails.getRole());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userDto
        ));
    }

    @GetMapping("/users")
    @Operation(summary = "Lister les utilisateurs", description = "Retourne la liste de tous les utilisateurs.")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", users
            ));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Procède à la déconnexion côté client en supprimant le JWT.")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token manquant"
            ));
        }

        String token = authHeader.substring(7);

        authService.blacklistToken(token);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Déconnexion réussie"
        ));
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Valider le token", description = "Vérifie si le token JWT actuel est valide.")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "valid", true
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "valid", false
        ));
    }

}