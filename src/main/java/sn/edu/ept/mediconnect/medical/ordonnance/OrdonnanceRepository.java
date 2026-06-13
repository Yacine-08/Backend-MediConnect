package sn.edu.ept.mediconnect.medical.ordonnance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    @Query("SELECT o FROM Ordonnance o LEFT JOIN FETCH o.lignes WHERE o.consultation.id = :consultationId")
    Optional<Ordonnance> findByConsultationId(@Param("consultationId") Long consultationId);

    boolean existsByConsultationId(Long consultationId);

    @Query("SELECT o FROM Ordonnance o LEFT JOIN FETCH o.lignes WHERE o.consultation.patient.id = :patientId")
    List<Ordonnance> findByConsultationPatientId(@Param("patientId") Long patientId);

    @Query("SELECT o FROM Ordonnance o LEFT JOIN FETCH o.lignes WHERE o.consultation.medecin.id = :medecinId")
    List<Ordonnance> findByConsultationMedecinId(@Param("medecinId") Long medecinId);
}
