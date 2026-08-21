package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.HttpHeaders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:securitytest;DB_CLOSE_DELAY=-1",
        "app.jwt.secret=test-secret-with-at-least-32-characters"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    private String bearer(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("andreas");
        user.setEmail("andreas@test.at");
        user.setPassword("$argon2id$platzhalter");
        user.setRole(role);
        user.setActive(true);

        return "Bearer " + tokenService.issue(user).value();
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cv/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void garbageTokenisUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cv/me").header("Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenPassesAuthentication() throws Exception {
        mockMvc.perform(get("/api/cv/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.TEILNEHMER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminEndpointRejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/participants")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.TEILNEHMER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointAcceptsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/participants")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicCardIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/card/muster"))
                .andExpect(status().isNotFound());
    }

    @Test
    void preflightFromAllowedOriginIsAnswered() throws Exception {
        mockMvc.perform(options("/api/cv/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    void preflightFromForeignOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/cv/me")
                        .header(HttpHeaders.ORIGIN, "http://ahwz.dev")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void actualRequestCarriesAllowOiriginHeader() throws Exception {
        mockMvc.perform(get("/api/cv/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Role.TEILNEHMER)))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081"));
    }

    @Test
    void preflightOutsideApiPathHasNoCorsHeaders() throws Exception {
        mockMvc.perform(options("/h2-console/login.do")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void preflightWithDisallowedMethodIsRejected() throws Exception {
        mockMvc.perform(options("/api/cv/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:8081")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH"))
                .andExpect(status().isForbidden());
    }
}