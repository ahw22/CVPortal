package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.EducationRequest;
import at.bbrz.cvportal.backend.dtos.EducationResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Education;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.EntryNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import at.bbrz.cvportal.backend.repositories.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository repository;
    private final CurriculumVitaeRepository cvRepository;
    private final CvMapper mapper;

    @Transactional(readOnly = true)
    public List<EducationResponse> findAll(UUID userId) {
        return repository.findByCv_User_IdOrderBySortOrderAsc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public EducationResponse create(UUID userId, EducationRequest request) {
        CurriculumVitae cv = cvRepository.findByUserId(userId)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf für Benutzer gefunden"));
        Education entry = new Education();

        entry.setCv(cv);
        apply(request, entry);
        if (request.sortOrder() == null) entry.setSortOrder(nextSortOrder(userId));

        cv.setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public EducationResponse update(UUID userId, Long entryId, EducationRequest request) {
        Education entry = new Education();

        apply(request, entry);
        entry.getCv().setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(entry);
    }

    @Transactional
    public void delete(UUID userId, Long entryId) {
        Education entry = findOwnEntry(userId, entryId);

        entry.getCv().setLastUpdated(LocalDateTime.now());
        repository.delete(entry);
    }

    private void apply(EducationRequest request, Education entry) {
        entry.setInstitution(request.institution().trim());
        entry.setDegree(request.degree().trim());
        entry.setFieldOfStudy(request.fieldOfStudy().trim());
        entry.setStartDate(request.startDate());
        entry.setEndDate(request.endDate());
        if (request.sortOrder() != null) entry.setSortOrder(request.sortOrder());
    }

    private Education findOwnEntry(UUID userId, Long entryId) {
        return repository.findByIdAndCv_User_Id(entryId, userId)
                .orElseThrow(() -> new EntryNotFoundException("Ausbildung " + entryId + " nicht gefunden."));
    }

    private int nextSortOrder(UUID userId) {
        return repository.findFirstByCv_User_IdOrderBySortOrderDesc(userId)
                .map(entry -> entry.getSortOrder() + 1)
                .orElse(0);
    }
}
