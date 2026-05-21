package sn.edu.ept.mediconnect.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.edu.ept.mediconnect.auth.entities.OtpCode;
import sn.edu.ept.mediconnect.auth.entities.TypeOtp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    // Dernier OTP valide pour un utilisateur
    @Query("""
        SELECT o FROM OtpCode o
        WHERE o.userId = :uid
        AND o.type = :type
        AND o.used = false
        AND o.expireA > :maintenant
        ORDER BY o.createdAt DESC
        LIMIT 1
        """)
    Optional<OtpCode> findLastValidOtp(
        @Param("uid")       Long uid,
        @Param("type") TypeOtp type,
        @Param("maintenant") LocalDateTime maintenant
    );

    // Invalider tous les anciens OTP d'un utilisateur
    @Modifying
    @Query("""
        UPDATE OtpCode o SET o.used = true
        WHERE o.userId = :uid
        AND o.type = :type
        """)
    void invalidateLastOtp(
        @Param("uid") Long uid,
        @Param("type") TypeOtp type
    );

    // Nettoyage des OTP expirés (tâche planifiée)
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expireA < :maintenant")
    void deleteExpired(@Param("maintenant") LocalDateTime maintenant);
}