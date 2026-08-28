package at.bbrz.cvportal.backend.exceptions;

/**
 * Lebenslauf existiert ist aber nicht oeffentlich freigegeben.
 */
public class CvNotPublicException extends RuntimeException {
    public CvNotPublicException() {
        super("Profil nicht öffentlich");
    }
}
