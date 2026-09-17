package at.bbrz.cvportal.frontend.dtos;

/**
 * Antwort von {@code GET /api/card/{username}} (F04).
 */
public record CardResponse(
        String username,
        String firstName,
        String lastName,
        String jobTitle,
        String summary,
        String email,
        String phone,
        String profilePhotoBase64,
        boolean cvPublic) {
}