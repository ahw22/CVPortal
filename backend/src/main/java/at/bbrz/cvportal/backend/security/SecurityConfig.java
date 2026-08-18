package at.bbrz.cvportal.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String PATH_H2_CONSOLE = "/h2-console/**";
    private static final String PATH_ADMIN = "/api/admin/**";
    private static final String PATH_REGISTER = "/api/auth/register";
    private static final String PATH_LOGIN = "/api/auth/login";
    private static final String PATH_PUBLIC_CARD = "/api/card/**";
    private static final String PATH_PUBLIC_CV = "/api/cv/public/**";


    /**
     * Definiert die Filterkette fuer alle Requests.
     * @param http der von Spring bereitgestellte Builder
     * @return die konfigurierte Filterkette
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                // Kein Cookie, keine Session daher kein Angriffsvektor fuer CSRF.
                .csrf(csrf -> csrf.disable())
                // Liest die CorsConfigurationSource-Bean
                // Wichtig: der CorsFilter laeuft vor der Authentifizierung und beantwortet den
                // OPTIONS-Preflight selbst - sonst kaeme dort ein 401 zurueck.
                .cors(Customizer.withDefaults())
                // Kein Security Context da REST api stateless ist.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Damit die H2-Konsole im Frameset rendert
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PATH_REGISTER, PATH_LOGIN).permitAll()
                        .requestMatchers(HttpMethod.GET, PATH_PUBLIC_CARD, PATH_PUBLIC_CV).permitAll()
                        .requestMatchers(PATH_H2_CONSOLE).permitAll()
                        .requestMatchers(PATH_ADMIN).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();

    }

    /**
     * Der Encoder mit dem Passwoerter gehasht und geprueft werden.
     *
     * @return Argon2idEncoder mit Standardwerten
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
