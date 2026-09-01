package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EducationRepository extends JpaRepository<Education, Long> {
    Optional<Education> findByIdAndCv_User_Id(Long id, UUID userId);

    List<Education> findByCv_User_IdOrderBySortOrderAsc(UUID userId);

    Optional<Education> findFirstByCv_User_IdOrderBySortOrderDesc(UUID userId);
}
