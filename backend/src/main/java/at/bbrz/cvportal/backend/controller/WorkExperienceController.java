package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.WorkExperienceRequest;
import at.bbrz.cvportal.backend.dtos.WorkExperienceResponse;
import at.bbrz.cvportal.backend.services.WorkExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv/me/work-experience")
@RequiredArgsConstructor
public class WorkExperienceController {

    private final WorkExperienceService service;

    @GetMapping
    public List<WorkExperienceResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return service.findAll(userId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkExperienceResponse create(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody WorkExperienceRequest request) {
        return service.create(userId(jwt), request);
    }

    @PutMapping("/{id}")
    public WorkExperienceResponse update(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Long id,
                                         @Valid @RequestBody WorkExperienceRequest request) {
        return service.update(userId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt,
                       @PathVariable Long id) {
        service.delete(userId(jwt), id);
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
