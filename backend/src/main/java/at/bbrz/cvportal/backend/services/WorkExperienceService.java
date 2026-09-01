package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.WorkExperienceRequest;
import at.bbrz.cvportal.backend.dtos.WorkExperienceResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.WorkExperience;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.EntryNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import at.bbrz.cvportal.backend.repositories.WorkExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class WorkExperienceService {

    private final WorkExperienceRepository repository;
    private final CurriculumVitaeRepository cvRepository;
    private final CvMapper mapper;

    @Transactional(readOnly = true)
    public List<WorkExperienceResponse> findAll(UUID userId) {
        return repository.findByCv_User_IdOrderBySortOrderAsc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public WorkExperienceResponse create(UUID userId, WorkExperienceRequest request) {
        CurriculumVitae cv = cvRepository.findByUserId(userId)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf für Benutzer gefunden"));
        WorkExperience entry = new WorkExperience();

        entry.setCv(cv);
        apply(request, entry);
        if (request.sortOrder() == null) entry.setSortOrder(nextSortOrder(userId));

        cv.setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public WorkExperienceResponse update(UUID userId, Long entryId, WorkExperienceRequest request) {
        WorkExperience entry = findOwnEntry(userId, entryId);

        apply(request, entry);
        entry.getCv().setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public void delete(UUID userId, Long entryId) {
        WorkExperience entry = findOwnEntry(userId, entryId);

        entry.getCv().setLastUpdated(LocalDateTime.now());
        repository.delete(entry);
    }

    private void apply(WorkExperienceRequest request, WorkExperience entry) {
        entry.setCompany(request.company().trim());
        entry.setPosition(request.position().trim());
        entry.setStartDate(request.startDate());
        entry.setEndDate(request.endDate());
        entry.setDescription(hasText(request.description()) ? request.description().trim() : null);
        if (request.sortOrder() != null) entry.setSortOrder(request.sortOrder());
    }

    private WorkExperience findOwnEntry(UUID userId, Long entryId) {
        return repository.findByIdAndCv_User_Id(entryId, userId)
                .orElseThrow(() -> new EntryNotFoundException("Berufserfahrung " + entryId + " nicht gefunden"));
    }

    private int nextSortOrder(UUID userId) {
        return repository.findFirstByCv_User_IdOrderBySortOrderDesc(userId)
                .map(entry -> entry.getSortOrder() + 1)
                .orElse(0);
    }
}
