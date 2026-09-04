package at.bbrz.cvportal.backend.dtos;

import jakarta.validation.constraints.NotNull;

public record ActiveUpdateRequest(
        @NotNull Boolean active) {
}
