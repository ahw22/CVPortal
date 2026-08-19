package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.repositories.UserRepository;
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

    private User save(String username, Role role, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.at");
        user.setPassword("$argon2id$platzhalter");
        user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
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

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("Andreas"));
    }

    @Test
    void blankUsernameThrowsInsteadOfReturningAnyUser() {
        save("andreas", Role.TEILNEHMER, true);

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(""));
    }

    @Test
    void inactiveUserIsLoadedButReportedAsDisabled() {
        save("gesperrt", Role.TEILNEHMER, false);

        UserDetails details = userDetailsService.loadUserByUsername("gesperrt");

        assertFalse(details.isEnabled());
    }

    @Test
    void credentialsArePassedThroughUNchanged() {
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