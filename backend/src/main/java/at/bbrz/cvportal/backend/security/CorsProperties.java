package at.bbrz.cvportal.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Konfiguration der CORS Einstellungen aus dem {@code app.cors} namespace
 *
 * @param allowedOrigins Herkuenfte denen der Browser Zugriff erlaubt
 */
@ConfigurationProperties("app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
