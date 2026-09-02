package at.bbrz.cvportal.backend.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record EducationRequest(
        @NotBlank
        @Size(max = 150)
        String institution,
        @NotBlank
        @Size(max = 100)
        String degree,
        @Size(max = 100)
        String fieldOfStudy,
        @NotNull
        @PastOrPresent
        LocalDate startDate,
        LocalDate endDate,
        Integer sortOrder) {

    @AssertTrue(message = "Enddatum darf nicht vor dem Startdatum liegen")
    public boolean isEndDateNotBeforeStartDate() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
