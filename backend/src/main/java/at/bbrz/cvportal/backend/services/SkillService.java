package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.SkillRequest;
import at.bbrz.cvportal.backend.dtos.SkillResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Skill;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.EntryNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import at.bbrz.cvportal.backend.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository repository;
    private final CurriculumVitaeRepository cvRepository;
    private final CvMapper mapper;

    @Transactional(readOnly = true)
    public List<SkillResponse> findAll(UUID userId) {
        return repository.findByCv_User_IdOrderBySortOrderAsc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public SkillResponse create(UUID userId, SkillRequest request) {
        CurriculumVitae cv = cvRepository.findByUserId(userId)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf für Benutzer gefunden"));
        Skill entry = new Skill();

        entry.setCv(cv);
        apply(request, entry);
        if (request.sortOrder() == null) entry.setSortOrder(nextSortOrder(userId));

        cv.setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public SkillResponse update(UUID userId, Long entryId, SkillRequest request) {
        Skill entry = findOwnEntry(userId, entryId);

        apply(request, entry);
        entry.getCv().setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public void delete(UUID userId, Long entryId) {
        Skill entry = findOwnEntry(userId, entryId);

        entry.getCv().setLastUpdated(LocalDateTime.now());
        repository.delete(entry);
    }

    private void apply(SkillRequest request, Skill entry) {
        entry.setName(request.name().trim());
        entry.setLevel(request.level());
        if (request.sortOrder() != null) entry.setSortOrder(request.sortOrder());
    }

    private Skill findOwnEntry(UUID userId, Long entryId) {
        return repository.findByIdAndCv_User_Id(entryId, userId)
                .orElseThrow(() -> new EntryNotFoundException("Kompetenz " + entryId + " nicht gefunden"));
    }

    private int nextSortOrder(UUID userId) {
        return repository.findFirstByCv_User_IdOrderBySortOrderDesc(userId)
                .map(entry -> entry.getSortOrder() + 1)
                .orElse(0);
    }
}
