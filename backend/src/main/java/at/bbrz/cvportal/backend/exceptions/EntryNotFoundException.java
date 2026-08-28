package at.bbrz.cvportal.backend.exceptions;

/**
 * Ein Eintrag (WorkExperience, Skill etc.) existiert nicht im Lebenslauf des angefragten Benutzers.
 */
public class EntryNotFoundException extends RuntimeException {
    public EntryNotFoundException(String message) {
        super(message);
    }
}
