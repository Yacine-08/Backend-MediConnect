package sn.edu.ept.mediconnect.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.edu.ept.mediconnect.common.entities.OrdreMedecin;


@Repository
public interface OrdreMedecinRepository extends JpaRepository<OrdreMedecin, Long> {

    boolean existsByNumOrdre(String numOrdre);
}