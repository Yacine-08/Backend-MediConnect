package sn.edu.ept.mediconnect.users.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByNumPatient(String numPatient);

    List<Patient> findByCreeParId(Long createurId);

    List<Patient> findByActif(Boolean actif);

    boolean existsByNumPatient(String numPatient);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "p.numPatient LIKE CONCAT('%', :terme, '%') OR " +
            "LOWER(p.telephone) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Patient> search(@Param("terme") String terme);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.creePar.id = :createurId")
    long countByCreateur(@Param("createurId") Long createurId);

    // Patients qui ont eu au moins une consultation dans la même spécialité
    // Subquery évite le problème de jointure JOINED-inheritance (m1_1 missing)
    @Query("SELECT DISTINCT c.patient FROM Consultation c " +
           "WHERE c.medecin IN (SELECT m FROM Medecin m WHERE m.specialite = :specialite)")
    List<Patient> findByMedecinSpecialite(@Param("specialite") String specialite);

    @Query("SELECT DISTINCT c.patient FROM Consultation c " +
           "WHERE c.medecin IN (SELECT m FROM Medecin m WHERE m.specialite = :specialite) " +
           "AND (LOWER(c.patient.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "     LOWER(c.patient.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
           "     c.patient.numPatient LIKE CONCAT('%', :terme, '%') OR " +
           "     LOWER(c.patient.telephone) LIKE LOWER(CONCAT('%', :terme, '%')))")
    List<Patient> searchByMedecinSpecialite(@Param("terme") String terme,
                                            @Param("specialite") String specialite);
}
