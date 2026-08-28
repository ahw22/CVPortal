package at.bbrz.cvportal.backend.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionManager extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CvNotFoundException.class)
    public ProblemDetail handleCvNotFound(CvNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Lebenslauf nicht gefunden", e.getMessage());
    }

    @ExceptionHandler(CvNotPublicException.class)
    public ProblemDetail handleCvNotPublic(CvNotPublicException e) {
        return problem(HttpStatus.FORBIDDEN, "Profil nicht öffentlich", e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Anmeldung fehlgeschlagen", e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException e) {
        return problem(HttpStatus.CONFLICT, "Benutzer existiert bereits", e.getMessage());
    }

    /**
     * Ein eingeloggter Benutzer versucht ohne passende Rolle (z.B. Ein Teilnehmer versucht auf das Admin dashboard zuzugreifen)
     * muss explizit behandelt werden. Ansonsten wuerde der Default Fall angewendet werden und ein 500 statt einem 403
     * returned
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, "Zugriff verweigert", "Für diese Aktion fehlt die Berechtigung");
    }

    @ExceptionHandler(EntryNotFoundException.class)
    public ProblemDetail handleEntryNotFound(EntryNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Eintrag nicht gefunden", e.getMessage());
    }

    /**
     * Bean Validation: 400 mit Feldliste damit das Frontend die Felder markieren kann
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST,
                "Validierungsfehler",
                "Die Eingabe ist unvollständig oder ungültig");
        body.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Default Handler. Alles unerwartete wird geloggt. Nach aussen geht eine neutrale Meldung
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexcpected(Exception e) {
        log.error("Unerwarterter Fehler", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Interner Fehler",
                "Es ist ein unerwarteter Fehler aufgetreten");
    }


    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
