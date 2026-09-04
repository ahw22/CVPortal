package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.*;
import at.bbrz.cvportal.backend.entities.*;
import org.springframework.stereotype.Component;

/**
 * Wandelt die Unterelemente eines Lebenslaufs in Response DTOs um.
 */
@Component
public class CvMapper {

    public WorkExperienceResponse toResponse(WorkExperience entry) {
        return new WorkExperienceResponse(entry.getId(),
                entry.getCompany(),
                entry.getPosition(),
                entry.getStartDate(),
                entry.getEndDate(),
                entry.getDescription(),
                entry.getSortOrder());
    }

    public EducationResponse toResponse(Education entry) {
        return new EducationResponse(entry.getId(),
                entry.getInstitution(),
                entry.getDegree(),
                entry.getFieldOfStudy(),
                entry.getStartDate(),
                entry.getEndDate(),
                entry.getSortOrder());
    }

    public SkillResponse toResponse(Skill entry) {
        return new SkillResponse(entry.getId(),
                entry.getName(),
                entry.getLevel(),
                entry.getSortOrder());
    }

    public LanguageResponse toResponse(Language entry) {
        return new LanguageResponse(entry.getId(),
                entry.getLanguage(),
                entry.getLevel(),
                entry.getSortOrder());
    }

    public ParticipantResponse toResponse(CurriculumVitae cv, int completeness) {
        return new ParticipantResponse(
                cv.getUser().getUsername(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getJobTitle(),
                cv.getLastUpdated(),
                completeness,
                cv.getUser().isActive());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }
}
