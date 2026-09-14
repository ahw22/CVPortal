package at.bbrz.cvportal.frontend.dtos;

public record UserResponse(String id, String username, String email, String role, boolean active) {
}
