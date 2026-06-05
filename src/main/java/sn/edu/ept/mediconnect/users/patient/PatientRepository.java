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

    List<Patient> findByCreeParId(Long infirmierId);

    List<Patient> findByActif(Boolean actif);

    boolean existsByNumPatient(String numPatient);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "p.numPatient LIKE CONCAT('%', :terme, '%') OR " +
            "LOWER(p.telephone) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Patient> search(@Param("terme") String terme);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.creePar.id = :infirmierId")
    long countByInfirmier(@Param("infirmierId") Long infirmierId);
}
