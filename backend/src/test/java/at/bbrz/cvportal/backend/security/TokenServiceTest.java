package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private static final String SECRET = "test-secret-with-at-least-32-characters";

    // Bewusst nicht der Standard
    private static final int EXPIRATION_HOURS = 3;

    private TokenService tokenService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        JwtProperties properties = new JwtProperties(SECRET, EXPIRATION_HOURS);
        SecretKey key = config.jwtSecretKey(properties);

        tokenService = new TokenService(config.jwtEncoder(key), properties);
        decoder = config.jwtDecoder(key);
    }

    private User newUser(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("andreas");
        user.setEmail("andreas@test.at");
        user.setPassword("$argon2id$platzhalter");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    @Test
    void subjectIsUserIdAndUsernameIsASeparateClaim() {
        User user = newUser(Role.TEILNEHMER);

        Jwt jwt = decoder.decode(tokenService.issue(user).value());

        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals("andreas", jwt.getClaimAsString(TokenService.CLAIM_USERNAME));
    }

    @Test
    void roleClaimUsesTheEnumConstantName() {
        Jwt jwt = decoder.decode(tokenService.issue(newUser(Role.ADMIN)).value());

        assertEquals("ADMIN", jwt.getClaimAsString(TokenService.CLAIM_ROLE));
    }

    @Test
    void tokenLifeTimeMatchesConfig() {
        Jwt jwt = decoder.decode(tokenService.issue(newUser(Role.TEILNEHMER)).value());

        long seconds = Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toSeconds();

        assertEquals(EXPIRATION_HOURS * 60 * 60L, seconds);
    }

    @Test
    void returnedExpiryMatchesTheTokenClaim() {
        IssuedToken issued = tokenService.issue(newUser(Role.TEILNEHMER));

        Jwt jwt = decoder.decode(issued.value());

        // JWT speichert Zeitstempel nur auf Sekundengenauigkeit
        assertEquals(issued.expiresAt().truncatedTo(ChronoUnit.SECONDS), jwt.getExpiresAt());
    }

    @Test
    void unsavedUserIsRejected() {
        User user = newUser(Role.TEILNEHMER);
        user.setId(null);

        assertThrows(NullPointerException.class, () -> tokenService.issue(user));
    }

    @Test
    void freshTokenIsNotExpired() {
        Jwt jwt = decoder.decode(tokenService.issue(newUser(Role.TEILNEHMER)).value());

        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
    }
}