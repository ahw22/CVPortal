package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LanguageRequest(
        @NotBlank
        @Length(max = 60)
        String language,
        @NotNull
        LanguageLevel level,
        Integer sortOrder
) {
}
