package sn.edu.ept.mediconnect.medical.transfert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransfertRepository extends JpaRepository<Transfert, Long> {

    List<Transfert> findByPatientId(Long patientId);

    List<Transfert> findByMedecinId(Long medecinId);

    List<Transfert> findByHopitalSourceId(String hopitalSourceId);

    List<Transfert> findByHopitalDestinationId(String hopitalDestinationId);

    List<Transfert> findByStatut(StatutTransfert statut);
}