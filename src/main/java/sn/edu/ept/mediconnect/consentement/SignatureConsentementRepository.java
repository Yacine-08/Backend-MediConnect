package sn.edu.ept.mediconnect.consentement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignatureConsentementRepository extends JpaRepository<SignatureConsentement, Long> {

    List<SignatureConsentement> findByPatientId(Long patientId);

    // Décision d'un patient pour un type de consentement précis
    @Query("""
        SELECT s FROM SignatureConsentement s
        WHERE s.patient.id      = :patientId
          AND s.consentement.typeConsentement = :type
    """)
    Optional<SignatureConsentement> findByPatientIdAndType(
            @Param("patientId") Long patientId,
            @Param("type")      TypeConsentement type);

    // Vérifie qu'un patient a accepté un type de consentement
    @Query("""
        SELECT COUNT(s) > 0 FROM SignatureConsentement s
        WHERE s.patient.id      = :patientId
          AND s.consentement.typeConsentement = :type
          AND s.accepte = true
    """)
    boolean patientAAccepte(
            @Param("patientId") Long patientId,
            @Param("type")      TypeConsentement type);
}