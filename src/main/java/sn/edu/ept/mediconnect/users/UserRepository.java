package sn.edu.ept.mediconnect.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelephone(String telephone);
    Optional<User> findByEmailOrTelephone(String email, String telephone);
    boolean existsByEmailOrTelephone(String email, String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
