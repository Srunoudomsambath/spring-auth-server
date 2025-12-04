package co.istad.authserver.feature.role;

import co.istad.authserver.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRole(String role);

    boolean existsByRole(String role);
}
