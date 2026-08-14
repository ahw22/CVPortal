package at.bbrz.cvportal.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    private static final String SECRET = "test-secret-with-at-least-32-characters";

    private JwtConfig config;
    private JwtEncoder encoder;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        config = new JwtConfig();
        SecretKey key = config.jwtSecretKey(new JwtProperties(SECRET, 8));
        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key);
    }

    /* Stellt token aus dessen Erstelldatum und Auslaufdatum frei waehlbar ist */
    private String tokenExpiringAt(Instant issuedAt, Instant expiresAt, String subject) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("role", "TEILNEHMER")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Test
    void encodedTokenCanBeDecodedAgain() {
        String subject = UUID.randomUUID().toString();
        String token = tokenExpiringAt(Instant.now(), Instant.now().plus(8, ChronoUnit.HOURS), subject);

        Jwt decoded = decoder.decode(token);

        assertEquals(subject, decoded.getSubject());
        assertEquals("TEILNEHMER", decoded.getClaimAsString("role"));
        assertEquals("HS256", decoded.getHeaders().get("alg").toString());
    }

    @Test
    void tamperedSignatureIsRejected() {
        String token = tokenExpiringAt(Instant.now(), Instant.now().plus(8, ChronoUnit.HOURS), "irrelevant");

        int signatureStart = token.lastIndexOf('.') + 1;
        char originalChar = token.charAt(signatureStart);
        String tampered = token.substring(0, signatureStart) + (originalChar == 'A' ? 'B' : 'A') + token.substring(signatureStart + 1);

        assertThrows(JwtException.class, () -> decoder.decode(tampered));
    }

    @Test
    void expiredTokenIsRejected() {
        Instant now = Instant.now();
        String token = tokenExpiringAt(now.minus(9, ChronoUnit.HOURS), now.minus(1, ChronoUnit.HOURS), "irrelevant");

        assertThrows(JwtValidationException.class, () -> decoder.decode(token));
    }

}