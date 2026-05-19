package sn.edu.ept.mediconnect.users;

import jakarta.persistence.*;
import lombok.*;
import sn.edu.ept.mediconnect.common.entities.Adresse;
import sn.edu.ept.mediconnect.common.entities.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_db")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 20)
    private String telephone;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "mfa_actif", nullable = false)
    private Boolean mfaActif = false;

    @Column(name = "mfa_secret", length = 100)
    private String mfaSecret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
