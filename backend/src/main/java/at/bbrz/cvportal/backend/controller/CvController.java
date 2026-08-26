package at.bbrz.cvportal.backend.controller;


import at.bbrz.cvportal.backend.dtos.CvResponse;
import at.bbrz.cvportal.backend.dtos.CvUpdateRequest;
import at.bbrz.cvportal.backend.dtos.CvVisibilityRequest;
import at.bbrz.cvportal.backend.services.CvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvService service;

    @GetMapping("/me")
    public CvResponse getOwnCv(@AuthenticationPrincipal Jwt jwt) {
        return service.getOwnCv(userId(jwt));
    }

    @PutMapping("/me")
    public CvResponse updateOwnCv(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CvUpdateRequest request) {
        return service.updateOwnCv(userId(jwt), request);
    }

    /** Wird per Browser-fetch() gecalled und loest den CORS-Preflight aus */
    @PutMapping("/me/visibility")
    public CvResponse updateVisibility(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CvVisibilityRequest request) {
        return service.updateVisibility(userId(jwt), request.publicVisible());
    }

    @GetMapping("/public/{username}")
    public CvResponse getPublicCv(@PathVariable String username) {
        return service.getPublicCv(username);
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

}
