package at.bbrz.cvportal.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lebenslauf existiert ist aber nicht oeffentlich freigegeben.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class CvNotPublicException extends RuntimeException {
    public CvNotPublicException() {
        super("Profil nicht öffentlich");
    }
}
