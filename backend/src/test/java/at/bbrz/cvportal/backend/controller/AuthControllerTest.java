package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.AuthResponse;
import at.bbrz.cvportal.backend.dtos.LoginRequest;
import at.bbrz.cvportal.backend.dtos.RegisterRequest;
import at.bbrz.cvportal.backend.dtos.UserResponse;
import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.exceptions.InvalidCredentialsException;
import at.bbrz.cvportal.backend.exceptions.UserAlreadyExistsException;
import at.bbrz.cvportal.backend.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String REGISTER_JSON = """
            {
                "username":"andreas",
                "email":"andreas@test.at",
                "password":"geheim12345"
            }
            """;
    private static final String LOGIN_JSON = """
            {
                "username":"andreas",
                "password":"geheim12345"
            }
            """;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReturns201AndTheCreatedUser() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(
                        "01a023c5-659b-7648-b092-f6582274f84e",
                        "andreas",
                        "andreas@test.at",
                        Role.TEILNEHMER));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("01a023c5-659b-7648-b092-f6582274f84e"))
                .andExpect(jsonPath("$.username").value("andreas"))
                .andExpect(jsonPath("$.email").value("andreas@test.at"))
                .andExpect(jsonPath("$.role").value("TEILNEHMER"));
    }

    @Test
    void registerPassesTheRequestBodyToTheService() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse("1",
                        "andreas",
                        "andreas@test.at",
                        Role.TEILNEHMER));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isCreated());

        verify(authService).register(new RegisterRequest("andreas", "andreas@test.at", "geheim12345"));
    }

    @Test
    void registerWithBlankUsernameIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"",
                                "email":"andreas@test.at",
                                "password":"geheim12345"
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void registerWithBadEmailIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"andreas",
                                "email":"keine-email",
                                "password":"geheim12345"
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void registerWithTooShortPasswordIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"andreas",
                                "email":"andreas@test.at",
                                "password":"kurz"
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void registerWithTooLongPasswordIsRejectedBeforeTheService() throws Exception {
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"andreas",
                                "email":"andreas@test.at",
                                "password":\"""" + loremIpsum + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void registerWithIllegalCharactersInUsernameIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username":"test asdf",
                                "email":"andreas@test.at",
                                "password":"geheim12345"
                                }"""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void registerWithMalformedJsonIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void duplicateRegistrationBecomes409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Benutzername bereits vergeben"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturns200AndTheToken() throws Exception {
        Instant expiresAt = Instant.now().plus(8, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("header.payload.signature", expiresAt, "andreas", Role.TEILNEHMER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("header.payload.signature"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.username").value("andreas"))
                .andExpect(jsonPath("$.role").value("TEILNEHMER"));
    }

    @Test
    void loginWithBlankPasswordIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "username": "andreas",
                                "password": "";
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void wrongCredentialsBecome401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isUnauthorized());
    }
}










