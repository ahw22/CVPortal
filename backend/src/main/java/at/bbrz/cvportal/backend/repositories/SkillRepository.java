package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByIdAndCv_User_Id(Long id, UUID userId);

    List<Skill> findByCv_User_IdOrderBySortOrderAsc(UUID userId);

    Optional<Skill> findFirstByCv_User_IdOrderBySortOrderDesc(UUID userId);
}
