package at.bbrz.cvportal.backend.exceptions;

public class CvNotFoundException extends RuntimeException {
    public CvNotFoundException(String message) {
        super(message);
    }
}
