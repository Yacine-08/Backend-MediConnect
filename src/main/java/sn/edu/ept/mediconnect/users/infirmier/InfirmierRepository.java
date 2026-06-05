package sn.edu.ept.mediconnect.users.infirmier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfirmierRepository extends JpaRepository<Infirmier, Long> {
    Optional<Infirmier> findByEmail(String email);

    Optional<Infirmier> findByTelephone(String telephone);

    List<Infirmier> findByHopitalNom(String nomHopital);

    List<Infirmier> findByActif(Boolean actif);

    @Query("SELECT i FROM Infirmier i WHERE " +
            "LOWER(i.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(i.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(i.email) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Infirmier> search(@Param("terme") String terme);
}
