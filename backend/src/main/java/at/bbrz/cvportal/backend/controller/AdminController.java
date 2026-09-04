package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.ActiveUpdateRequest;
import at.bbrz.cvportal.backend.dtos.ParticipantResponse;
import at.bbrz.cvportal.backend.dtos.RoleUpdateRequest;
import at.bbrz.cvportal.backend.dtos.UserResponse;
import at.bbrz.cvportal.backend.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @GetMapping("/participants")
    public List<ParticipantResponse> findAllParticipants() {
        return service.findAllParticipants();
    }

    @GetMapping("/users")
    public List<UserResponse> findAllUsers() {
        return service.findAllUsers();
    }

    @PutMapping("/users/{id}/active")
    public UserResponse setActive(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID id,
                                  @Valid @RequestBody ActiveUpdateRequest request) {
        return service.setActive(adminId(jwt), id, request.active());
    }

    @PutMapping("/users/{id}/role")
    public UserResponse setRole(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable UUID id,
                                @Valid @RequestBody RoleUpdateRequest request) {
        return service.setRole(adminId(jwt), id, request.role());
    }

    private UUID adminId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
