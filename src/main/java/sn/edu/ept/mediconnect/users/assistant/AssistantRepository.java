package sn.edu.ept.mediconnect.users.assistant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant, Long> {

    Optional<Assistant> findByEmail(String email);

    Optional<Assistant> findByTelephone(String telephone);

    List<Assistant> findByActif(Boolean actif);

    @Query("SELECT a FROM Assistant a WHERE " +
            "LOWER(a.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(a.prenom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :terme, '%'))")
    List<Assistant> search(@Param("terme") String terme);
}
