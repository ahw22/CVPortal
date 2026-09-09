package at.bbrz.cvportal.frontend.security;

import java.io.Serializable;
import java.time.Instant;

public record ApiUser(
        String username,
        String role,
        String token,
        Instant expiresAt
) implements Serializable {

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
