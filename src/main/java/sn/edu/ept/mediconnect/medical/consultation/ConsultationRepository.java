package sn.edu.ept.mediconnect.medical.consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByPatientId(Long patientId);

    List<Consultation> findByMedecinId(Long medecinId);

    List<Consultation> findByInfirmierId(Long infirmierId);

    List<Consultation> findByStatut(StatutConsultation statut);

    Optional<Consultation> findByRendezVousId(Long rendezVousId);

    List<Consultation> findByPatientIdAndStatut(Long patientId, StatutConsultation statut);

    List<Consultation> findByMedecinIdAndStatut(Long medecinId, StatutConsultation statut);
}