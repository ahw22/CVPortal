package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.LanguageRequest;
import at.bbrz.cvportal.backend.dtos.LanguageResponse;
import at.bbrz.cvportal.backend.services.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv/me/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService service;

    @GetMapping
    public List<LanguageResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return service.findAll(userId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LanguageResponse create(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody LanguageRequest request) {
        return service.create(userId(jwt), request);
    }

    @PutMapping("/{id}")
    public LanguageResponse update(@AuthenticationPrincipal Jwt jwt,
                                   @PathVariable Long id,
                                   @Valid @RequestBody LanguageRequest request) {
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
