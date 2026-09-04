package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull Role role) {
}
