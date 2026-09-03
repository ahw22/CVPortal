package at.bbrz.cvportal.backend.dtos;

public record CardResponse(
        String username,
        String firstName,
        String lastName,
        String jobTitle,
        String summary,
        String email,
        String phone,
        String profilePhotoBase64,
        boolean cvPublic
) {
}
