package at.bbrz.cvportal.frontend.dtos;

import java.time.LocalDateTime;

public record ParticipantResponse(
        String username,
        String firstName,
        String lastName,
        String jobTitle,
        LocalDateTime lastUpdated,
        int completeness,
        boolean active) {
}
