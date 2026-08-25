package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CurriculumVitaeRepository extends JpaRepository<CurriculumVitae, Long> {
    Optional<CurriculumVitae> findByUserUsername(String username);
    Optional<CurriculumVitae> findByUserId(UUID userId);
}
