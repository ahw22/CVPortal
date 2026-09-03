package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank
        @Size(max = 60)
        String name,
        @NotNull
        SkillLevel level,
        Integer sortOrder) {

}
