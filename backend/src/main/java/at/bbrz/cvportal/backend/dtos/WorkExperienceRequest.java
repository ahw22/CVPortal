package at.bbrz.cvportal.backend.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Anlegen oder aendern einer Station im Berufsleben.
 * {@code endDate == null} bedeutet bis dato.
 * {@code sortOrder == null} haengt den Eintrag hinten an.
 */
public record WorkExperienceRequest(
        @NotBlank
        @Size(max = 100)
        String company,
        @NotBlank
        @Size(max = 100)
        String position,
        @NotNull
        @PastOrPresent
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 2000)
        String description,
        Integer sortOrder) {


    @AssertTrue(message = "Enddatum darf nicht vor dem Startdatum liegen")
    public boolean isEndDateNotBeforeStartDate() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
