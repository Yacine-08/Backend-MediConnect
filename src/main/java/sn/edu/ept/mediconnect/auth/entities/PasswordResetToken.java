package sn.edu.ept.mediconnect.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity                                    // ← added
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id                                    // ← now from jakarta.persistence
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiryDate;
    private boolean used;
}