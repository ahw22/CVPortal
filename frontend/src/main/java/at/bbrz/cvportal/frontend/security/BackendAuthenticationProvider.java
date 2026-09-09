package at.bbrz.cvportal.frontend.security;

import at.bbrz.cvportal.frontend.dtos.AuthResponse;
import at.bbrz.cvportal.frontend.dtos.LoginRequest;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import at.bbrz.cvportal.frontend.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authentifiziert gegen das Backend statt eigener Auth.
 * <p>
 * Das Frontend hat keine DB, Passwörter werden nur im Backend geprüft. Dieser Provider steht an der Stelle der
 * Kette an der {@code DaoAuthenticationProvider} steht. er ruft {@code /api/auth/login} auf und legt das JWT in das
 * {@link ApiUser} Principal an. Spring Security schreibt den SecurityContext in die Session. Damit liegt das Token
 * Serverseitig und erreicht den Browser nie.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackendAuthenticationProvider implements AuthenticationProvider {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String MSG_BAD_CREDENTIALS = "Benutzername oder Passwort ist falsch";

    private final ApiClientService apiClient;

    /**
     * @param authentication der noch nicht authentifizierte Token aus dem Login-Formular
     * @return ein authentifizierter Token mit {@link ApiUser} als Principal
     * @throws BadCredentialsException          bei HTTP 401 - bewusst ohne Hinweis welches feld falsch war.
     * @throws AuthenticationServiceException   wenn das Backend nicht antwortet oder unerwartet reagiert.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        try {
            AuthResponse response = apiClient.login(new LoginRequest(username, password));

            ApiUser principal = new ApiUser(
                    response.username(), response.role(), response.token(), response.expiresAt());

            log.debug("Login erfolgreich für {} (Rolle {}), Token läuft ab {}",
                    response.username(),
                    response.role(),
                    response.expiresAt());

            return UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(ROLE_PREFIX + response.role())));
        } catch (ApiException e) {
            if (e.is(HttpStatus.UNAUTHORIZED)) {
                throw new BadCredentialsException(MSG_BAD_CREDENTIALS);
            }
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
