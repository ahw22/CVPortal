package at.bbrz.cvportal.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfiguration der JWT-Einstellungen aus dem {@code app.jwt} namespace
 * @param secret            gemeinsames Geheimnis fuer HS256, mindestens 32 Bytes
 * @param expirationHours   Gueltigkeitsdauer eines Tokens
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(
        String secret,
        int expirationHours
) {
}
