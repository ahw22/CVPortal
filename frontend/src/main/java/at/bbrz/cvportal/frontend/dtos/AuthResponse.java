package at.bbrz.cvportal.frontend.dtos;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        String username,
        String role) {
}
