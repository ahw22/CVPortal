package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Neue Station im Berufsleben. {@code endDate == null} bedeutet "bis heute".
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkExperienceForm {
    private String company;
    private String position;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    private String description;
}