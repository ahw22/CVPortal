package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Neue Ausbildungsstation. {@code endDate == null} bedeutet "laufend".
 */
@Getter
@Setter
@NoArgsConstructor
public class EducationForm {
    private String institution;
    private String degree;
    private String fieldOfStudy;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}