package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    Optional<Language> findByIdAndCv_User_Id(Long id, UUID userId);

    List<Language> findByCv_User_IdOrderBySortOrderAsc(UUID userId);

    Optional<Language> findFirstByCv_User_IdOrderBySortOrderDesc(UUID userId);
}
