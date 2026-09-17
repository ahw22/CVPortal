package at.bbrz.cvportal.frontend.service;

import at.bbrz.cvportal.frontend.dtos.*;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiClientService {

    private static final String MSG_UNREACHABLE = "Der Server ist zurzeit nicht erreichbar. Bitte versuchen Sie es später erneut.";

    private final RestTemplate restTemplate;

    public AuthResponse login(LoginRequest request) {
        return call("POST /api/auth/login",
                () -> restTemplate.postForObject("/api/auth/login", request, AuthResponse.class));
    }

    public UserResponse register(RegisterForm form) {
        return call("POST /api/auth/register",
                () -> restTemplate.postForObject("/api/auth/register", form, UserResponse.class));
    }

    public List<ParticipantResponse> findParticipants(String token) {
        return call("GET /api/admin/participants",
                () -> List.of(restTemplate.exchange("/api/admin/participants",
                        HttpMethod.GET,
                        new HttpEntity<>(headers(token)),
                        ParticipantResponse[].class).getBody()));
    }

    public List<UserResponse> findUsers(String token) {
        return call("GET /api/admin/users",
                () -> List.of(restTemplate.exchange("/api/admin/users",
                        HttpMethod.GET,
                        new HttpEntity<>(headers(token)),
                        UserResponse[].class).getBody()));
    }

    public void setActive(String userId, boolean active, String token) {
        call("PUT /api/admin/users/" + userId + "/active",
                () -> restTemplate.exchange("/api/admin/users/{id}/active", HttpMethod.PUT,
                        new HttpEntity<>(Map.of("active", active), headers(token)),
                        UserResponse.class, userId).getBody());
    }

    public void setRole(String userId, String role, String token) {
        call("PUT /api/admin/users/" + userId + "/role",
                () -> restTemplate.exchange("/api/admin/users/{id}/role", HttpMethod.PUT,
                        new HttpEntity<>(Map.of("role", role), headers(token)),
                        UserResponse.class, userId).getBody());
    }

    public CvResponse getOwnCv(String token) {
        return call("GET /api/cv/me",
                () -> restTemplate.exchange("/api/cv/me", HttpMethod.GET,
                        new HttpEntity<>(headers(token)), CvResponse.class).getBody());
    }

    public CvResponse updateOwnCv(CvForm form, String token) {
        return call("PUT /api/cv/me",
                () -> restTemplate.exchange("/api/cv/me", HttpMethod.PUT,
                        new HttpEntity<>(form, headers(token)), CvResponse.class).getBody());
    }

    public CardResponse getCard(String username) {
        return call("GET /api/card/" + username,
                () -> restTemplate.getForObject("/api/card/{u}", CardResponse.class, username));
    }

    public CvResponse getPublicCv(String username) {
        return call("GET /api/cv/public/" + username,
                () -> restTemplate.getForObject("/api/cv/public/{u}", CvResponse.class, username));
    }

    private <T> T call(String description, Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpStatusCodeException e) {
            ProblemDetail problem = problem(e);
            String detail = problem != null && problem.getDetail() != null
                    ? problem.getDetail()
                    : description + " -> " + e.getStatusCode();

            log.debug("Backend antwortete {} auf {}: {}", e.getStatusCode(), description, detail);
            throw new ApiException(e.getStatusCode(), detail, fieldErrors(problem), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot reach Backend bei {}", description, e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, MSG_UNREACHABLE, Map.of(), e);
        }
    }

    private Map<String, String> fieldErrors(ProblemDetail problem) {
        if (problem == null || problem.getProperties() == null) {
            return Map.of();
        }
        if (!(problem.getProperties().get("fieldErrors") instanceof Map<?, ?> errors)) {
            return Map.of();
        }
        Map<String, String> fields = new LinkedHashMap<>();
        errors.forEach((field, message) -> fields.put(String.valueOf(field), String.valueOf(message)));
        return Collections.unmodifiableMap(fields);
    }

    /* Liest das ProblemDetail aus der Antwort. {@code null} wenn kein Body */
    private ProblemDetail problem(HttpStatusCodeException e) {
        try {
            return e.getResponseBodyAs(ProblemDetail.class);
        } catch (RuntimeException ex) {
            log.warn("Antwort ist kein ProblemDetail: {}", e.getResponseBodyAsString(), ex);
            return null;
        }
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
