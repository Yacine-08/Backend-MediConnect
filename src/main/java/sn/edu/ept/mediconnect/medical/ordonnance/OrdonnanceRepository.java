package sn.edu.ept.mediconnect.medical.ordonnance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    Optional<Ordonnance> findByConsultationId(Long consultationId);

    boolean existsByConsultationId(Long consultationId);

    List<Ordonnance> findByConsultationPatientId(Long patientId);

    List<Ordonnance> findByConsultationMedecinId(Long medecinId);
}