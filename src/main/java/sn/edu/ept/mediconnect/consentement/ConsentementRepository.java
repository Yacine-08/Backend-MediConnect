package sn.edu.ept.mediconnect.consentement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsentementRepository extends JpaRepository<Consentement, Long> {

    Optional<Consentement> findByTypeConsentement(TypeConsentement typeConsentement);

    boolean existsByTypeConsentement(TypeConsentement typeConsentement);
}