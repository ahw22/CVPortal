package at.bbrz.cvportal.backend.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CvResponse(
        String username,
        String email,
        String firstName,
        String lastName,
        String jobTitle,
        String phone,
        String address,
        LocalDate birthDate,
        String summary,
        boolean publicVisible,
        int completeness,
        LocalDateTime lastUpdated,
        List<WorkExperienceResponse> workExperiences,
        List<EducationResponse> educations,
        List<SkillResponse> skills,
        List<LanguageResponse> languages) {
}
