package sn.edu.ept.mediconnect.medical.rendezvous;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByPatientId(Long patientId);

    List<RendezVous> findByMedecinId(Long medecinId);

    List<RendezVous> findByStatut(StatutRendezVous statut);

    List<RendezVous> findByMedecinIdAndStatut(Long medecinId, StatutRendezVous statut);

    List<RendezVous> findByPatientIdAndStatut(Long patientId, StatutRendezVous statut);

    boolean existsByPatientIdAndMedecinIdAndDateHeure(
            Long patientId, Long medecinId, LocalDateTime dateHeure);

    boolean existsByMedecinIdAndDateHeureBetweenAndStatutIn(
            Long medecinId, LocalDateTime debut, LocalDateTime fin, java.util.List<StatutRendezVous> statuts);

    List<RendezVous> findByMedecinIdAndDateHeureAfterAndStatutInOrderByDateHeureAsc(
            Long medecinId, LocalDateTime dateDebut, java.util.List<StatutRendezVous> statuts);
}
