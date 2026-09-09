package at.bbrz.cvportal.frontend.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatusCode status;
    private final Map<String, String> fieldErrors;

    public ApiException(HttpStatusCode status, String message, Map<String,String> fieldErrors, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.fieldErrors = fieldErrors == null ? Map.of() : fieldErrors;
    }

    public boolean is(HttpStatus expected) {
        return status.isSameCodeAs(expected);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
