package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private static final String HASH = "$argon2id$platzhalter";

    private User newUser(Role role, boolean active) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("andreas");
        user.setEmail("andreas@test.at");
        user.setPassword(HASH);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private List<String> authorityNames(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Test
    void adminGetsExactlyOnePrefixedAuthority() {
        UserPrincipal principal = new UserPrincipal(newUser(Role.ADMIN, true));

        assertEquals(List.of("ROLE_ADMIN"), authorityNames(principal));
    }

    @Test
    void teilnehmerGetsExactlyOnePrefixedAuthority() {
        UserPrincipal principal = new UserPrincipal(newUser(Role.TEILNEHMER, true));

        assertEquals(List.of("ROLE_TEILNEHMER"), authorityNames(principal));
    }

    @Test
    void authorityMatchesTheConfiguredPrefix() {
        UserPrincipal principal = new UserPrincipal(newUser(Role.ADMIN, true));

        assertTrue(authorityNames(principal).getFirst().startsWith("ROLE_"));
    }

    @Test
    void usernameAndPasswordAreDelegated() {
        User user = newUser(Role.TEILNEHMER, true);
        UserPrincipal principal = new UserPrincipal(user);

        assertEquals("andreas", principal.getUsername());
        assertEquals(HASH, principal.getPassword());
    }

    @Test
    void isEnabledMirrorsTheActiveFlag() {
        assertTrue(new UserPrincipal(newUser(Role.TEILNEHMER, true)).isEnabled());
        assertFalse(new UserPrincipal(newUser(Role.TEILNEHMER, false)).isEnabled());
    }

    @Test
    void accountStateFlagsUseTheInterfaceDefaults() {
        UserPrincipal principal = new UserPrincipal(newUser(Role.TEILNEHMER, true));

        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
    }

    @Test
    void wrappedEntityIsAccessible() {
        User user = newUser(Role.TEILNEHMER, true);

        assertSame(user, new UserPrincipal(user).getUser());
    }

    @Test
    void toStringDoesNotLeakThePasswordHash() {
        String text = new UserPrincipal(newUser(Role.TEILNEHMER, true)).toString();

        assertFalse(text.contains(HASH));
        assertTrue(text.contains("andreas"));
        assertTrue(text.contains("TEILNEHMER"));
    }
}