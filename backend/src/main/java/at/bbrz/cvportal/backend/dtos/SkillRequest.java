package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SkillRequest(
        @NotBlank
        @Length(max = 60)
        String name,
        @NotNull
        SkillLevel level,
        Integer sortOrder) {

}
