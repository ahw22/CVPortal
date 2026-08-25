package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:authflowtest;DB_CLOSE_DELAY=-1",
        "app.jwt.secret=test-secret-with-at-least-32-characters"
})
public class AuthFlowIntegrationTest {

    private static final String USERNAME = "flow-testuser";
    private static final String PASSWORD = "geheim12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String registerJson(String username, String email) {
        return """
                {
                "username":"%s",
                "email":"%s",
                "password":"%s"
                }
                """.formatted(username, email, PASSWORD);
    }

    private void register(String username, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(username, email)))
                .andExpect(status().isCreated());
    }

    @Test
    void registeredUserCanLogInAndGetsAToken() throws Exception {
        register(USERNAME, "flow@test.at");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"%s",
                                "password":"%s"
                                }
                                """.formatted(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.role").value("TEILNEHMER"));
    }

    @Test
    void registrationPersistsHashedPasswordAndAnEmptyCv() throws Exception {
        register("flow-hash", "flow-hash@test.at");

        User saved = userRepository.findByUsername("flow-hash").orElseThrow();

        assertNotEquals(PASSWORD, saved.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, saved.getPassword()));
        assertNotNull(saved.getCurriculumVitae());
        assertNotNull(saved.getId());
    }

    @Test
    void emailIsStoredNormalized() throws Exception {
        register("flow-mail", "Flow.Mail@TEST.AT");

        User saved = userRepository.findByUsername("flow-mail").orElseThrow();

        assertEquals("flow.mail@test.at", saved.getEmail());
    }

    @Test
    void secondRegistrationWithTheSameUsernameIs409() throws Exception {
        register("flow-dup", "flow-dup@test.at");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("flow-dup", "andere@test.at")))
                .andExpect(status().isConflict());
    }

    @Test
    void secondRegistrationWithTheSameEmailInDifferentCaseIs409() throws Exception {
        register("flow-mail-a", "gleiche@test.at");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("flow-mail-b", "gleiche@test.at")))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordIs401() throws Exception {
        register("flow-wrong", "flow-wrong@test.at");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"flow-wrong",
                                "password":"falsch12345"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownUsernameIs401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"gibt-es-nicht",
                                "password":"geheim12345"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginToDeactivatedUserIs401() throws Exception {
        register("flow-deactivated", "flow-deactivated@test.at");

        User saved = userRepository.findByUsername("flow-deactivated").orElseThrow();
        saved.setActive(false);
        userRepository.saveAndFlush(saved);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"flow-deactivated",
                                "password":"%s"
                                }
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnauthorized());
    }
}










