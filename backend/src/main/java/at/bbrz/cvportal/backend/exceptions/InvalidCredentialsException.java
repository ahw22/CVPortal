package at.bbrz.cvportal.backend.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Benutzername oder Passwort falsch");
    }
}
