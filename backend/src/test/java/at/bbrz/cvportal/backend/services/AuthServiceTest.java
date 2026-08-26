package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.AuthResponse;
import at.bbrz.cvportal.backend.dtos.LoginRequest;
import at.bbrz.cvportal.backend.dtos.RegisterRequest;
import at.bbrz.cvportal.backend.dtos.UserResponse;
import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.exceptions.InvalidCredentialsException;
import at.bbrz.cvportal.backend.exceptions.UserAlreadyExistsException;
import at.bbrz.cvportal.backend.repositories.UserRepository;
import at.bbrz.cvportal.backend.security.IssuedToken;
import at.bbrz.cvportal.backend.security.TokenService;
import at.bbrz.cvportal.backend.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final UUID SAVED_ID = UUID.fromString("01a0385f-b91c-721e-8af9-d0708cc1e3cd");
    private static final String PLAIN_PASSWORD = "geheim12345";
    private static final String HASH = "$argon2id$platzhalter";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterRequest registerRequest(String username, String email) {
        return new RegisterRequest(username, email, PLAIN_PASSWORD);
    }

    // Simuliert das Speichern in der DB da erst dann Hibernate ID vergibt
    private void stubSaveWithGeneratedId() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(SAVED_ID);
            return toSave;
        });
    }

    private User existingUser(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("andreas");
        user.setEmail("andreas@test.at");
        user.setPassword(HASH);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    @Test
    void registerStoresHashedPasswordAndNEverThePlaintext() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(HASH);
        stubSaveWithGeneratedId();

        authService.register(registerRequest("andreas", "andreas@test.at"));

        verify(userRepository).save(userCaptor.capture());
        assertEquals(HASH, userCaptor.getValue().getPassword());
        assertNotEquals(PLAIN_PASSWORD, userCaptor.getValue().getPassword());
    }

    @Test
    void registerAlwaysAssignsTeilnehmerAndActivatesTheAccount() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(HASH);
        stubSaveWithGeneratedId();

        authService.register(registerRequest("andreas", "andreas@test.at"));

        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.TEILNEHMER, userCaptor.getValue().getRole());
        assertTrue(userCaptor.getValue().isActive());
    }

    @Test
    void registerAttachesAnEmptyCvLinkedBackToUser() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(HASH);
        stubSaveWithGeneratedId();

        authService.register(registerRequest("andreas", "andreas@test.at"));

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertNotNull(saved.getCurriculumVitae());
        assertSame(saved, saved.getCurriculumVitae().getUser());
    }

    @Test
    void registerNormalizesTheEmailBeforeTheDuplicateCheckAndBeforeSaving() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(HASH);
        stubSaveWithGeneratedId();

        authService.register(registerRequest("andreas", "  Andreas@TEST.AT  "));

        verify(userRepository).existsByEmail("andreas@test.at");
        verify(userRepository).save(userCaptor.capture());
        assertEquals("andreas@test.at", userCaptor.getValue().getEmail());
    }

    @Test
    void registerMapsTheSavedEntityIntoTheResponse() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(false);
        when(passwordEncoder.encode(PLAIN_PASSWORD)).thenReturn(HASH);
        stubSaveWithGeneratedId();

        UserResponse response = authService.register(registerRequest("andreas", "andreas@test.at"));

        assertEquals(SAVED_ID.toString(), response.id());
        assertEquals("andreas", response.username());
        assertEquals("andreas@test.at", response.email());
        assertEquals(Role.TEILNEHMER, response.role());
    }

    @Test
    void registerWithTakenUsernameThrowsAndSavesNothing() {
        when(userRepository.existsByUsername("andreas")).thenReturn(true);

        UserAlreadyExistsException existsException = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest("andreas", "andreas@test.at"));
        });

        assertEquals("Benutzername bereits vergeben", existsException.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void takenUsernameShortCircuitsBeforeTheEmailCheck() {
        when(userRepository.existsByUsername("andreas")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest("andreas", "andreas@test.at")));

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void registerWithTakenEmailThrowsAndSavesNothing() {
        when(userRepository.existsByUsername("andreas")).thenReturn(false);
        when(userRepository.existsByEmail("andreas@test.at")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(registerRequest("andreas", "andreas@test.at"));
        });

        assertEquals("E-Mail-Adresse ist bereits registriert", exception.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginPassesTheSubmittedCredentialsToTheManager() {
        User user = existingUser(Role.TEILNEHMER);
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(user));
        when(tokenService.issue(user)).thenReturn(new IssuedToken("header.payload.signature", Instant.now()));

        authService.login(new LoginRequest("andreas", PLAIN_PASSWORD));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("andreas", captor.getValue().getPrincipal());
        assertEquals(PLAIN_PASSWORD, captor.getValue().getCredentials());
        assertFalse(captor.getValue().isAuthenticated());
    }

    @Test
    void loginReturnsTokenExpiryUsernameAndRole() {
        Instant expiresAt = Instant.now().plus(8, ChronoUnit.HOURS);
        User user = existingUser(Role.ADMIN);
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(user));
        when(tokenService.issue(user)).thenReturn(new IssuedToken("header.payload.signature", expiresAt));

        AuthResponse response = authService.login(new LoginRequest("andreas", PLAIN_PASSWORD));

        assertEquals("header.payload.signature", response.token());
        assertEquals(expiresAt, response.expiresAt());
        assertEquals("andreas", response.username());
        assertEquals(Role.ADMIN, response.role());
    }

    @Test
    void loginTakesTheEntityFromTheAuthenticationAndNeverHitsTheRepository() {
        User user = existingUser(Role.TEILNEHMER);
        when(authenticationManager.authenticate(any())).thenReturn(authenticated(user));
        when(tokenService.issue(any(User.class))).thenReturn(new IssuedToken("t", Instant.now()));

        authService.login(new LoginRequest("andreas", PLAIN_PASSWORD));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(tokenService).issue(captor.capture());
        assertSame(user, captor.getValue());
        verifyNoInteractions(userRepository);
    }

    @Test
    void wrongPasswordBecomesInvalidCredentialsAndIssuesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad Credentials"));

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(new LoginRequest("andreas", "falsch12345"));
        });

        verifyNoInteractions(tokenService);
    }

    @Test
    void disabledAccountBecomesInvalidCredentialsAndLeaksNoDetail() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("User is disabled"));

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("gesperrt", PLAIN_PASSWORD)));

        // Gleiche Meldung bie falschem Passwort
        assertEquals("Benutzername oder Passwort falsch", exception.getMessage());
        verifyNoInteractions(tokenService);
    }

    @Test
    void unexpectedPrincipalTypeIsAConfigErrorNotAnInputError() {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated("andreas", null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        assertThrows(IllegalStateException.class,
                () -> authService.login(new LoginRequest("andreas", PLAIN_PASSWORD)));

        verifyNoInteractions(tokenService);
    }

    private Authentication authenticated(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }
}