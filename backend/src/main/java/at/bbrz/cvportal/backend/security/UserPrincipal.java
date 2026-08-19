package at.bbrz.cvportal.backend.security;

import at.bbrz.cvportal.backend.entities.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter zwischen der JPA Entity {@link  User} und dem, was Spring Security erwartet.
 * <p>
 * Wird ausschliesslich auf dem Login-Path vom {@code DaoAuthenticationProvider} verwendet.
 * Alle anderen Requests authentifizieren sich mit dem Bearer Token. Dort baut Spring die Authorities
 * aus dem {@code role} claim (konfiguriert im {@code application.yaml}.
 * <p>
 * Die gekapselte Entity ist nach dem Laden detatched. Es duerfen deshalb nur die skalaren Felder gelesen werden.
 * z.B. {@code getCurriculumVitae()} wuerde eine {@code LazyInitializationException} werfen.
 */
public class UserPrincipal implements UserDetails {
    // Muss zum {@code authority-prefix} in appplication.yaml passen
    private static final String ROLE_PREFIX = "ROLE_";
    @Getter
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "username='" + user.getUsername() + "'" +
                "role='" + user.getRole().name() + "'";
    }
}
