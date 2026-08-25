package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.AuthResponse;
import at.bbrz.cvportal.backend.dtos.LoginRequest;
import at.bbrz.cvportal.backend.dtos.RegisterRequest;
import at.bbrz.cvportal.backend.dtos.UserResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.exceptions.InvalidCredentialsException;
import at.bbrz.cvportal.backend.exceptions.UserAlreadyExistsException;
import at.bbrz.cvportal.backend.repositories.UserRepository;
import at.bbrz.cvportal.backend.security.IssuedToken;
import at.bbrz.cvportal.backend.security.TokenService;
import at.bbrz.cvportal.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    /**
     * Legt ein neues Konto samt leerem Lebenslauf an.
     * <p>
     * Die Rolle wird hier gesetzt und nicht aus dem Request uebernommen damit nicht jeder sich zum Admin machen kann.
     *
     * @param request die geprueften Registrierungsdaten
     * @return der angelegte User
     * @throws UserAlreadyExistsException wenn Username oder E-Mail bereits vergeben sind.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Benutzername bereits vergeben");
        }

        // Die Entity normalisiert die E-Mail erst in @PrePersist. Also muss hier geprueft werden
        String email = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("E-Mail-Adresse ist bereits registriert");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.TEILNEHMER);
        user.setActive(true);
        // CascadeType.ALL auf der Beziehung speichert den leeren Lebenslauf mit
        user.setCurriculumVitae(new CurriculumVitae());

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId().toString(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    /**
     * Prueft die Anmeldedaten unst stellt bei Erfolg einen Token aus.
     *
     * @param request Username und Passwort
     * @return Token, Ablaufzeitpunkt, Username und Rolle
     * @throws InvalidCredentialsException bei falschen Daten oder deaktiviertem Konto
     * @throws IllegalStateException       wenn der Principal kein {@link UserPrincipal} ist. Das setzt voraus dass,
     *                                     die Filterkette mit einem anderen {@code AuthenticationProvider} konfiguriert wurde.
     *                                     Daher Konfig-Fehler und kein Eingabefehler.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(),
                    request.password()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Unerwarteter Principal-Typ: " + authentication.getPrincipal()
                    .getClass()
                    .getName());
        }

        User user = principal.getUser();
        IssuedToken token = tokenService.issue(user);

        return new AuthResponse(token.value(), token.expiresAt(), user.getUsername(), user.getRole());
    }
}
