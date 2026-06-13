package sn.edu.ept.mediconnect.users.medecin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    List<Medecin> findByActif(Boolean actif);

    List<Medecin> findByDisponibleTrueAndActifTrue();

    @Query("SELECT m FROM Medecin m WHERE " +
            "LOWER(m.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(m.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Medecin> search(@Param("terme") String terme);
}
