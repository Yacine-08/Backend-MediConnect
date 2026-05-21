package sn.edu.ept.mediconnect.users.medecin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardiologueRepository extends JpaRepository<Cardiologue, Long> {
}
