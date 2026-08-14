package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;

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

}