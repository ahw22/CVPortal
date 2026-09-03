package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LanguageRequest(
        @NotBlank
        @Size(max = 60)
        String language,
        @NotNull
        LanguageLevel level,
        Integer sortOrder
) {
}
