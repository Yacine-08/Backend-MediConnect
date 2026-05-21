package sn.edu.ept.mediconnect.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.edu.ept.mediconnect.common.entities.Hopital;

import java.util.Optional;

@Repository
public interface HopitalRepository extends JpaRepository<Hopital, Long> {
    Optional<Hopital> findByNom(String nom);
}
