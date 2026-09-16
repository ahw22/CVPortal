package at.bbrz.cvportal.frontend.dtos;

import java.time.LocalDate;

public record WorkExperienceResponse(Long id, String company, String position,
                                     LocalDate startDate, LocalDate endDate,
                                     String description, int sortOrder) {
}