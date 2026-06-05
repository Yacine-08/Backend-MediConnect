package sn.edu.ept.mediconnect.medical.ordonnance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LignePrescriptionRepository extends JpaRepository<LignePrescription, Long> {

    List<LignePrescription> findByOrdonnanceId(Long ordonnanceId);
}