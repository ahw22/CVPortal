package at.bbrz.cvportal.frontend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String PATH_LOGIN = "/login";
    private static final String PATH_REGISTER = "/register";
    private static final String PATH_LOGOUT = "/logout";
    private static final String PATH_DASHBOARD = "/dashboard";
    private static final String PATH_CV_EDIT = "/cv/edit";
    private static final String PATH_PUBLIC_CV = "/cv/*";
    private static final String PATH_PUBLIC_CARD = "/card/*";
    private static final String PATH_ADMIN = "/admin/**";
    private static final String PATH_ACCESS_DENIED = "/error/403";
    private static final String PATH_ERROR = "/error";

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String[] PATHS_STATIC = {
            "/css/**", "/js/**", "/vendor/**", "/favicon.ico"
    };


    /**
     * @param http          der Builder von Spring
     * @param provider      prüft die Daten gegen das Backend
     * @return die konfigurierte Filterkette
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BackendAuthenticationProvider provider) {
        return http
                .authenticationProvider(provider)

                // CSRF bleibt activ. Thymeleaf hängt das versteckte Fled bei jedem Formular mit th:action an
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PATHS_STATIC).permitAll()
                        .requestMatchers(PATH_ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, PATH_LOGIN, PATH_REGISTER).permitAll()
                        .requestMatchers(HttpMethod.POST, PATH_LOGIN, PATH_REGISTER).permitAll()

                        // /cv/edit muss vor /cv/* stehen da sonst die wildcard zuerst gilt
                        .requestMatchers(PATH_CV_EDIT).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_PUBLIC_CV, PATH_PUBLIC_CARD).permitAll()

                        .requestMatchers(PATH_ADMIN).hasRole(ROLE_ADMIN)
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage(PATH_LOGIN)
                        .loginProcessingUrl(PATH_LOGIN)
                        .defaultSuccessUrl(PATH_DASHBOARD)
                        .failureUrl(PATH_LOGIN + "?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl(PATH_LOGOUT)
                        .logoutSuccessUrl(PATH_LOGIN + "?logout")
                        // Logout löscht die Session und auch das JWT im SecurityContext
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.newSession())
                        .invalidSessionUrl(PATH_LOGIN)
                )

                .exceptionHandling(ex -> ex.accessDeniedPage(PATH_ACCESS_DENIED))
                .build();
    }
}
