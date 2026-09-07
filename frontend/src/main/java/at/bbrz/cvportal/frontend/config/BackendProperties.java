package at.bbrz.cvportal.frontend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Die Base-URL des Backends aus der application.yaml. Gegenstück zu {@code app.cors.allowed-origins} im Backend.
 */
@ConfigurationProperties(prefix = "app.backend")
public record BackendProperties(String baseUrl) {
}
