package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurriculumVitaeRepository extends JpaRepository<CurriculumVitae, Long> {
    Optional<CurriculumVitae> findByUserUsername(String username);
    Optional<CurriculumVitae> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    List<CurriculumVitae> findByUserRoleOrderByUserUsernameAsc(Role role);
}
