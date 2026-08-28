package at.bbrz.cvportal.backend.exceptions;

/**
 * Registrierung mit einem Usernamen oder E-Mail die es schon gibt.
 */
public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
