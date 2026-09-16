package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Die Stammdaten des Lebenslaufs. Formular-Bean und Request-Body fÜr
 * {@code PUT /api/cv/me}.
 * <p>
 * Kein Record: Thymeleaf bindet Formulare ueber JavaBeans-Setter.
 * <p>
 * Alle Felder sind optional, der Lebenslauf entsteht leer und wächst. Geprüft wird im
 * Backend ({@code CvUpdateRequest}), hier stehen deshalb keine Constraints.
 */
@Getter
@Setter
@NoArgsConstructor
public class CvForm {

    private String firstName;
    private String lastName;
    private String jobTitle;
    private String phone;
    private String address;

    /**
     * {@code <input type="date">} liefert ISO (yyyy-MM-dd).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    private String summary;

    /**
     * Fuellt das Formular aus dem geladenen Lebenslauf.
     */
    public static CvForm von(CvResponse cv) {
        CvForm form = new CvForm();
        form.firstName = cv.firstName();
        form.lastName = cv.lastName();
        form.jobTitle = cv.jobTitle();
        form.phone = cv.phone();
        form.address = cv.address();
        form.birthDate = cv.birthDate();
        form.summary = cv.summary();
        return form;
    }
}