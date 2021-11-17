package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    boolean existsById(UUID id);
}
