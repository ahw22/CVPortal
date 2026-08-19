package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.Role;


import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        String username,
        Role role) {
}
