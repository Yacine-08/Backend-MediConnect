package sn.edu.ept.mediconnect.medical.alerte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByPatientId(Long patientId);

    List<Alerte> findByConsultationId(Long consultationId);

    List<Alerte> findByNiveau(NiveauAlerte niveau);

    List<Alerte> findByAcquittee(Boolean acquittee);

    List<Alerte> findByPatientIdAndAcquittee(Long patientId, Boolean acquittee);
}