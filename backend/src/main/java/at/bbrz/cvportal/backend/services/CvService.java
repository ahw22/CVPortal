package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.*;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.CvNotPublicException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class CvService {

    private static final int CRITERIA_COUNT = 10;

    private final CurriculumVitaeRepository repository;

    /**
     * Fetched den eigenen Lebenslauf
     *
     * @param userId die userID aus dem {@code sub} claim des Tokens
     * @return CvResponse des Lebenslaufs
     * @throws CvNotFoundException wenn zum User kein Lebenslauf Existiert
     */
    @Transactional(readOnly = true)
    public CvResponse getOwnCv(UUID userId) {
        return toResponse(loadOwnCv(userId));
    }

    /**
     * Uebernimmt die Stammdaten aus dem request und liefert den aktuellen Stand zurueck damit das Frontend
     * keinen GET nach dem PUT benoetigt.
     */
    @Transactional
    public CvResponse updateOwnCv(UUID userId, CvUpdateRequest request) {
        CurriculumVitae cv = loadOwnCv(userId);

        cv.setFirstName(trimToNull(request.firstName()));
        cv.setLastName(trimToNull(request.lastName()));
        cv.setJobTitle(trimToNull(request.jobTitle()));
        cv.setPhone(trimToNull(request.phone()));
        cv.setAddress(trimToNull(request.address()));
        cv.setBirthDate(request.birthDate());
        cv.setSummary(trimToNull(request.summary()));

        return toResponse(cv);
    }

    /**
     * Schaltet die oeffentliche SSichtbarkeit um.
     * <p>
     * Dieser Endpunkt wird als einziger direkt per Browser-{@code fetch()} aufgerufen und erzeugt
     * damit den CORS-Preflight.
     */
    @Transactional
    public CvResponse updateVisibility(UUID userId, boolean publicVisible) {
        CurriculumVitae cv = loadOwnCv(userId);
        cv.setPublicVisible(publicVisible);
        return toResponse(cv);
    }

    /**
     * Liefert einen freigegebenen Lebenslauf ohne Anmeldung.
     *
     * @throws CvNotFoundException wenn es den Benutzer nicht gibt oder er deaktiviert wurde.
     * @throws CvNotPublicException wenn der Lebenslauf auf privat gesetzt ist.
     */
    @Transactional(readOnly = true)
    public CvResponse getPublicCv(String username) {
        CurriculumVitae cv = repository.findByUserUsername(username)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf fuer " + username));

        if (!cv.getUser().isActive()) {
            throw new CvNotFoundException("Kein Lebenslauf fuer " + username);
        }
        if (!cv.isPublicVisible()) {
            throw new CvNotPublicException();
        }
        return toResponse(cv);
    }

    /**
     * Anteil der erfuellten Kriterien in Prozent.
     * <p>
     * Zehn gleich gewichtete Kriterien: sechs Stammdatenfelder und je ein
     * Eintrag in Berufserfahrung, Ausbildung, Kenntnissen und Sprachen.
     * Leerer Lebenslauf ergibt 0, vollstaendiger 100.
     *
     * @param cv der zu bewertende Lebenslauf
     * @return Wert zwischen 0 und 100
     */
    public int calculateCompleteness(CurriculumVitae cv) {
        int fulfilled = 0;

        if (hasText(cv.getFirstName())) fulfilled++;
        if (hasText(cv.getLastName())) fulfilled++;
        if (hasText(cv.getJobTitle())) fulfilled++;
        if (hasText(cv.getPhone())) fulfilled++;
        if (hasText(cv.getAddress())) fulfilled++;
        if (cv.getBirthDate() != null) fulfilled++;
        if (!cv.getWorkExperiences().isEmpty()) fulfilled++;
        if (!cv.getEducations().isEmpty()) fulfilled++;
        if (!cv.getSkills().isEmpty()) fulfilled++;
        if (!cv.getLanguages().isEmpty()) fulfilled++;

        return fulfilled * 100 / CRITERIA_COUNT;
    }

    private String trimToNull(String string) {
        return hasText(string) ? string.trim() : null;
    }

    private CurriculumVitae loadOwnCv(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new CvNotFoundException("Kein Lebenslauf fuer Benutzer" + userId));
    }

    private CvResponse toResponse(CurriculumVitae cv) {
        User user = cv.getUser();

        List<WorkExperienceResponse> work = cv.getWorkExperiences().stream()
                .map(w -> new WorkExperienceResponse(w.getId(),
                        w.getCompany(),
                        w.getPosition(),
                        w.getStartDate(),
                        w.getEndDate(),
                        w.getDescription(),
                        w.getSortOrder()))
                .toList();
        List<EducationResponse> education = cv.getEducations().stream()
                .map(e -> new EducationResponse(e.getId(),
                        e.getInstitution(),
                        e.getDegree(),
                        e.getFieldOfStudy(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getSortOrder()))
                .toList();
        List<SkillResponse> skills = cv.getSkills().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getLevel()))
                .toList();
        List<LanguageResponse> languages = cv.getLanguages().stream()
                .map(l -> new LanguageResponse(l.getId(), l.getLanguage(), l.getLevel()))
                .toList();

        return new CvResponse(
                user.getUsername(),
                user.getEmail(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getJobTitle(),
                cv.getPhone(),
                cv.getAddress(),
                cv.getBirthDate(),
                cv.getSummary(),
                cv.isPublicVisible(),
                calculateCompleteness(cv),
                cv.getLastUpdated(),
                work,
                education,
                skills,
                languages);
    }
}
