package at.bbrz.cvportal.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final String JCA_ALGORITHM = "HmacSHA256";
    private static final int MIN_KEY_LENGTH_BYTES = 32;

    /**
     * Baut den symmetrischen Signaturschluessel aus dem konfigurierten Geheimnis
     *
     * @param properties die JWT Konfiguration
     * @return der Schluessel fuer Signatur und Verifikation
     * @throws IllegalStateException wenn das Geheimnis kuerzer als 32 zeichen ist
     */
    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < MIN_KEY_LENGTH_BYTES) {
            throw new IllegalStateException("app.jwt.secret ist %d Bytes lang, muss aber mindestens %d Bytes lang sein".formatted(
                    keyBytes.length,
                    MIN_KEY_LENGTH_BYTES));
        }
        return new SecretKeySpec(keyBytes, JCA_ALGORITHM);
    }

    /**
     * Stellt den Encoder bereit, der Claims yu einem signierten Token macht.
     *
     * @param jwtSecretKey der Signaturschluessel
     * @return der konfigurierte Encoder
     */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey));
    }

    /**
     * Stellt den Decoder bereit, der eingehende Tokens verifiziert.
     * <p>
     * Das Mac-Verfahren wird explizit festgelegt; der Decoder akzeptiert damit ausschliesslich
     * {@code alg: HS256} und weist abweichende Header ab, bevor Signatur geprueft wird
     *
     * @param jwtSecretKey der Signaturschluessel
     * @return der konfigurierte Decoder
     */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        return decoder;
    }
}
