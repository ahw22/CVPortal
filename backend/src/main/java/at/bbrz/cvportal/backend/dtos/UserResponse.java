package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.Role;

public record UserResponse(String id, String username, String email, Role role, boolean active) {
}
