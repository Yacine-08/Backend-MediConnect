package sn.edu.ept.mediconnect.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.edu.ept.mediconnect.common.entities.Adresse;

import java.util.List;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {
    List<Adresse> findByRegionAndDepartementAndCommune(
            String region,
            String departement,
            String commune
    );

    List<Adresse> findByRegion(String region);

    List<Adresse> findByCommune(String commune);

    List<Adresse> findByDepartement(String departement);
}
