package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.LanguageRequest;
import at.bbrz.cvportal.backend.dtos.LanguageResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Language;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.EntryNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import at.bbrz.cvportal.backend.repositories.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository repository;
    private final CurriculumVitaeRepository cvRepository;
    private final CvMapper mapper;

    @Transactional(readOnly = true)
    public List<LanguageResponse> findAll(UUID userId) {
        return repository.findByCv_User_IdOrderBySortOrderAsc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public LanguageResponse create(UUID userId, LanguageRequest request) {
        CurriculumVitae cv = cvRepository.findByUserId(userId)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf für Benutzer gefunden"));
        Language entry = new Language();

        entry.setCv(cv);
        apply(request, entry);
        if (request.sortOrder() == null) entry.setSortOrder(nextSortOrder(userId));

        cv.setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public LanguageResponse update(UUID userId, Long entryId, LanguageRequest request) {
        Language entry = findOwnEntry(userId, entryId);

        apply(request, entry);
        entry.getCv().setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(repository.save(entry));
    }

    @Transactional
    public void delete(UUID userId, Long entryId) {
        Language entry = findOwnEntry(userId, entryId);

        entry.getCv().setLastUpdated(LocalDateTime.now());
        repository.delete(entry);
    }

    private void apply(LanguageRequest request, Language entry) {
        entry.setLanguage(request.language().trim());
        entry.setLevel(request.level());
        if (request.sortOrder() != null) entry.setSortOrder(request.sortOrder());
    }

    private Language findOwnEntry(UUID userId, Long entryId) {
        return repository.findByIdAndCv_User_Id(entryId, userId)
                .orElseThrow(() -> new EntryNotFoundException("Sprache " + entryId + " nicht gefunden"));
    }

    private int nextSortOrder(UUID userId) {
        return repository.findFirstByCv_User_IdOrderBySortOrderDesc(userId)
                .map(entry -> entry.getSortOrder() + 1)
                .orElse(0);
    }
}
