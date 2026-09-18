package at.bbrz.cvportal.frontend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Fängt jede {@link ApiException} ab, die ein Controller nicht selbst behandelt.
 * <p>
 * Ohne das hier landet jeder Backend-Fehler auf der Standard-Fehlerseite von Spring. Der
 * wichtigste Fall ist das abgelaufene JWT
 */
@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

    private static final String VIEW_FORBIDDEN = "error/403";
    private static final String VIEW_ERROR = "error/500";
    private static final String REDIRECT_LOGIN_EXPIRED = "redirect:/login?abgelaufen";

    private static final String COOKIE_SESSION = "JSESSIONID";
    private static final String ATTR_MESSAGE = "meldung";

    /**
     * @param e        der Fehler aus dem Backend-Aufruf
     * @param request  liefert den Pfad fuer das Log und die zu beendende Session
     * @param response traegt den HTTP-Status der Fehlerseite
     * @param model    nimmt die Meldung fuer die Fehlerseite auf
     * @return Weiterleitung auf die Anmeldung bei 401, sonst eine Fehlerseite
     */
    @ExceptionHandler(ApiException.class)
    public String handle(ApiException e, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (e.is(HttpStatus.UNAUTHORIZED)) {
            log.info("Backend lehnt das Token ab ({}), Session wird beendet", request.getRequestURI());
            beendeSession(request, response);
            return REDIRECT_LOGIN_EXPIRED;
        }

        if (e.is(HttpStatus.FORBIDDEN)) {
            log.warn("Backend verweigert den Zugriff auf {}", request.getRequestURI());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return VIEW_FORBIDDEN;
        }

        log.error("Backend-Aufruf fehlgeschlagen ({}): {}", request.getRequestURI(), e.getMessage());
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        model.addAttribute(ATTR_MESSAGE, e.getMessage());
        return VIEW_ERROR;
    }

    /**
     * Dieselben Handler, die auch {@code /logout} verwendet. Session und SecurityContext
     * räumen, sonst bleibt das tote JWT im Principal liegen. Das Cookie muss mit weg,
     * sonst schickt der Browser die ungültige JSESSIONID wieder mit und
     * {@code invalidSessionUrl} leitet auf {@code /login} um, noch bevor der Parameter
     * {@code abgelaufen} ankommt.
     */
    private void beendeSession(HttpServletRequest request, HttpServletResponse response) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        new CookieClearingLogoutHandler(COOKIE_SESSION).logout(request, response, authentication);
    }
}