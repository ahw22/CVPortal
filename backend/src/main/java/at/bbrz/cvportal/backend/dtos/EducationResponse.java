package at.bbrz.cvportal.backend.dtos;

import java.time.LocalDate;

public record EducationResponse(Long id, String institution, String degree,
                                String fieldOfStudy, LocalDate startDate,
                                LocalDate endDate, int sortOrder) {}
