package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Laedt Benutzer fuer den Login aus der Datenbank.
 * <p>
 * Die {@code UsernameNotFoundException} wird vom {@code DaoAuthenticationProvider} in eine {@code BadCredentialsException}
 * umgewandelt. Genau das macht "Benutzer existiert nicht" und "Falsches Passwort" ununterscheidbar. Die Meldung
 * darf deshalb niemals unveraendert an den Client gelangen.
 * <p>
 * Da diese Bean existiert, erzeugt Spring Boot keinen Standartbenutzer mehr und gibt beim Start kein
 * generiertes Passwort mehr aus.
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Sucht nach User anhand des username.
     * <p>
     * Die Suche ist Case-sensitiv passend zum Constraint {@code uk_user_username}.
     * @param username der eingegebene username
     * @return die Anmeldedaten des Users
     * @throws UsernameNotFoundException wenn kein User mit dem Namen existiert.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Kein Benutzer mit diesem Namen"));
    }
}
