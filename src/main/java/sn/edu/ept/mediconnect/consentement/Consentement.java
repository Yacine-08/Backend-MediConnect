package sn.edu.ept.mediconnect.consentement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consentements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consentement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consentement", nullable = false, length = 50, unique = true)
    private TypeConsentement typeConsentement;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(length = 10)
    private String version;

    /** true uniquement pour POLITIQUE_CONFIDENTIALITE. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean obligatoire = false;

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