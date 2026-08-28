package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    /** Id und UserId in einer abfrage damit die Besitzpruefung nicht vergessbar ist */
    Optional<WorkExperience> findByIdAndCv_User_Id(Long id, UUID userId);
    List<WorkExperience> findByCv_User_IdOrderBySortOrderAsc(UUID userId);
    Optional<WorkExperience> findFirstByCv_User_IdOrderBySortOrderDesc(UUID userId);

}
