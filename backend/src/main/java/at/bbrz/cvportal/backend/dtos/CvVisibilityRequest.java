package at.bbrz.cvportal.backend.dtos;

import jakarta.validation.constraints.NotNull;

public record CvVisibilityRequest(
        @NotNull
        Boolean publicVisible) {
}
