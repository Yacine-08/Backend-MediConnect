package sn.edu.ept.mediconnect.medical.examen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findByConsultationId(Long consultationId);

    List<Examen> findByConsultationPatientId(Long patientId);

    List<Examen> findByType(ExamenType type);

    List<Examen> findByStatut(StatutExamen statut);

    List<Examen> findByConsultationIdAndType(Long consultationId, ExamenType type);
}
