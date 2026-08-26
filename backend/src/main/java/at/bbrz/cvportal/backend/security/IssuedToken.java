package at.bbrz.cvportal.backend.security;

import java.time.Instant;

/**
 * Ergebnis einer Token-Ausstellung
 * @param value     der signierte Token
 * @param expiresAt Zeitpunkt, ab dem der Token abgelaufen ist
 */
public record IssuedToken (String value, Instant expiresAt) {
}
