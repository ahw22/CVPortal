package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Benutzerkonto des Systems (Admin/Berater oder Teilnehmer).
 * <p>
 * Enthaelt ausschliesslich Anmelde- und Rollendaten. Die fachlichen
 * Lebenslaufdaten liegen in {@link CurriculumVitae}.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 150)
    private String email;

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Argon2id-Hash, niemals das Klartextpasswort (M10). */
    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.TEILNEHMER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Der Lebenslauf des Benutzers. Die Fremdschluesselspalte liegt in
     * {@link CurriculumVitae}, daher ist diese Seite mit {@code mappedBy}
     * die inverse Seite der Beziehung.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private CurriculumVitae curriculumVitae;

    /**
     * Setzt den Erstellungszeitpunkt automatisch beim ersten Speichern und normalisiert E-Mail
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.email = normalizeEmail(this.email);
    }

    /**
     * Normalisiert E-Mail bei Update vor dem Speichern
     */
    @PreUpdate
    void onUpdate() {
        this.email = normalizeEmail(this.email);
    }

    /**
     * Verknuepft Benutzer und Lebenslauf auf beiden Seiten konsistent.
     *
     * @param cv der zuzuordnende Lebenslauf, darf {@code null} sein
     */
    public void setCurriculumVitae(CurriculumVitae cv) {
        this.curriculumVitae = cv;
        if (cv != null) {
            cv.setUser(this);
        }
    }
}
