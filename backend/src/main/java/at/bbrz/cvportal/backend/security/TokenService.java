package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Stellt signierte Zugriffstoken fuer angemeldete User aus.
 * <p>
 * Der Service prueft keine Anmeldedaten und laedt keine Benutzer. Es wird ausschliesslich ein bereits Authentifizierter
 * {@code User} in einen signierten Token uebersetzt.
 * Die verifikation des Tokens uebernimmt die Filterkette
 */
@Service
public class TokenService {

    //Definition der Claim namen
    static final String CLAIM_ROLE = "role";
    static final String CLAIM_USERNAME = "username";

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    /**
     * Stellt einen Token fuer den gegeben User aus.
     * <p>
     * Der Payload enthaelt keine sensiblen daten und ist deswegen nicht verschluesselt.
     * @param user der authentifizierte User
     * @return Token und Ablaufzeitpunkt
     * @throws NullPointerException wenn der Benutzer keine ID hat
     */
    public IssuedToken issue(User user) {
        Objects.requireNonNull(user.getId(), "Benutzer ohne ID - vor der Token-Ausstellung speichern");

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.expirationHours(), ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedToken(value, expiresAt);
    }
}
