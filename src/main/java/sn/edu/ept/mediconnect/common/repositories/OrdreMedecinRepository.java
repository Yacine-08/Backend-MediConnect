package sn.edu.ept.mediconnect.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.edu.ept.mediconnect.common.entities.OrdreMedecin;

import java.util.Optional;


@Repository
public interface OrdreMedecinRepository extends JpaRepository<OrdreMedecin, Long> {

    Optional<OrdreMedecin> findByNumOrdre(String numOrdre);

    boolean existsByNumOrdre(String numOrdre);

    // Vérification croisée : numéro + nom + prénom
    // Tolérance sur la casse pour le nom/prénom
    @Query("""
    SELECT o FROM OrdreMedecin o
    WHERE o.numOrdre = :numOrdre
    AND LOWER(o.nom) = LOWER(:nom)
    AND o.actif = true
    """)
    Optional<OrdreMedecin> verifier(
            @Param("numOrdre") String numOrdre,
            @Param("nom")      String nom
    );
}