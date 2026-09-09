package at.bbrz.cvportal.frontend.service;

import at.bbrz.cvportal.frontend.dtos.AuthResponse;
import at.bbrz.cvportal.frontend.dtos.LoginRequest;
import at.bbrz.cvportal.frontend.dtos.RegisterRequest;
import at.bbrz.cvportal.frontend.dtos.UserResponse;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiClientService {

    private static final String MSG_UNREACHABLE = "Der Server ist zurzeit nicht erreichbar. Bitte versuche es später erneut.";

    private final RestTemplate restTemplate;

    public AuthResponse login(LoginRequest request) {
        return call("POST /api/auth/login",
                () -> restTemplate.postForObject("/api/auth/login", request, AuthResponse.class));
    }

    public UserResponse register(RegisterRequest request) {
        return call("POST /api/auth/register",
                () -> restTemplate.postForObject("/api/auth/register", request, UserResponse.class));
    }

    private <T> T call(String description, Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpStatusCodeException e) {
            log.debug("Backend replied {} auf {}", e.getStatusCode(), description);
            throw new ApiException(e.getStatusCode(), description + " -> " + e.getStatusCode(), fieldErrors(e), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot reach Backend bei {}", description, e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, MSG_UNREACHABLE, Map.of(), e);
        }
    }

    private Map<String, String> fieldErrors(HttpStatusCodeException e) {
        try {
            ProblemDetail problemDetail = e.getResponseBodyAs(ProblemDetail.class);
            if (problemDetail == null || problemDetail.getProperties() == null) {
                return Map.of();
            }
            if (!(problemDetail.getProperties().get("fieldErrors") instanceof Map<?,?> errors)) {
                return Map.of();
            }
            Map<String, String> fields = new LinkedHashMap<>();
            errors.forEach((field, alert) -> fields.put(String.valueOf(field), String.valueOf(alert)));
            return Collections.unmodifiableMap(fields);
        } catch (RuntimeException ex) {
            log.warn("fieldErrors konnte nicht gelesen werden: {}", e.getResponseBodyAsString(), ex);
            return Map.of();
        }
    }
}
