package at.bbrz.cvportal.backend.dtos;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Stammdaten des Lebenslaufs. Alle Felder sind optional. Der Lebenslauf
 * entsteht leer und wird schrittweise befuellt. Der Vollstaendigkeitsgrad wird davon abgeleitet.
 */
public record CvUpdateRequest(
        @Size(max = 50)
        String firstName,
        @Size(max = 50)
        String lastName,
        @Size(max = 100)
        String jobTitle,
        @Size(max = 30)
        @Pattern(regexp = "|[0-9+/\\-() ]+", message = "Nur Ziffern und + / - ( ) erlaubt")
        String phone,
        @Size(max = 200)
        String address,
        @Past
        LocalDate birthDate,
        @Size(max = 1000)
        String summary) {
}
