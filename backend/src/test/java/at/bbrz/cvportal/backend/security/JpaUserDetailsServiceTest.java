package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:userdetailstest",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaUserDetailsService.class)
class JpaUserDetailsServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaUserDetailsService userDetailsService;

    @Autowired
    private EntityManager entityManager;

    private User save(String username, Role role, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.at");
        user.setPassword("$argon2id$platzhalter");
        user.setRole(role);
        user.setActive(active);
        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();
        return saved;
    }

    @Test
    void roleBecomesPrefixedAuthority() {
        save("admin", Role.ADMIN, true);

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertEquals(List.of("ROLE_ADMIN"), authorityNames(details));
    }

    @Test
    void teilnehmerBecomesPrefixedAuthority() {
        save("andreas", Role.TEILNEHMER, true);

        UserDetails details = userDetailsService.loadUserByUsername("andreas");

        assertEquals(List.of("ROLE_TEILNEHMER"), authorityNames(details));
    }

    @Test
    void entityIsReachableForTokenService() {
        User saved = save("andreas", Role.TEILNEHMER, true);

        UserDetails details = userDetailsService.loadUserByUsername("andreas");

        assertInstanceOf(UserPrincipal.class, details);
        assertEquals(saved.getId(), ((UserPrincipal) details).getUser().getId());
    }

    @Test
    void unknownUsernameThrows() {
        save("andreas", Role.TEILNEHMER, true);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("niemand"));
    }

    @Test
    void lookupIsCaseSensitive() {
        save("andreas", Role.TEILNEHMER, true);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("ANDREAS"));
    }

    @Test
    void blankUsernameThrowsInsteadOfReturningAnyUser() {
        save("andreas", Role.TEILNEHMER, true);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(""));
    }

    /**
     * Der Service filter deaktivierte User bewusst nicht. Er laedt sie und meldet sie uber isEnabled() als deaktiviert.
     * Die Ablehnung macht der DaoAuthentificationProvider mit einer DisabledException.
     */
    @Test
    void inactiveUserIsLoadedButReportedAsDisabled() {
        save("gesperrt", Role.TEILNEHMER, false);

        UserDetails details = userDetailsService.loadUserByUsername("gesperrt");

        assertFalse(details.isEnabled());
    }

    @Test
    void credentialsArePassedThroughUnchanged() {
        save("andreas", Role.TEILNEHMER, true);

        UserDetails details = userDetailsService.loadUserByUsername("andreas");

        assertEquals("andreas", details.getUsername());
        assertEquals("$argon2id$platzhalter", details.getPassword());
    }

    private List<String> authorityNames(UserDetails details) {
        return details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

}