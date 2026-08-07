package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lebenslauf eines Teilnehmers (1:1 zu {@link User}).
 * <p>
 * Die Stammdatenfelder sind bewusst nullable: Ein Lebenslauf entsteht leer und
 * wird schrittweise befuellt (F02). Der Vollstaendigkeitsgrad wird daraus
 * berechnet.
 */
@Entity
@Table(name = "curriculum_vitae", uniqueConstraints =
        @UniqueConstraint(name = "uk_cv_user", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class CurriculumVitae {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Besitzende Seite der 1:1-Beziehung, haelt die Fremdschluesselspalte. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cv_user"))
    private User user;

    @Column(length = 100)
    private String jobTitle;

    @Column(length = 30)
    private String phone;

    @Column(length = 200)
    private String address;

    private LocalDate birthDate;

    /** Kurzprofil, erscheint auf der Visitenkarte (F04). */
    @Column(length = 1000)
    private String summary;

    /** Profilfoto als Base64 (W03), optional. */
    @Lob
    private String profilePhotoBase64;

    /**
     * Steuert, ob {@code /cv/{username}} oeffentlich abrufbar ist (M07).
     * Opt-in: Default {@code false} gemaess Datenschutzkonzept.
     */
    @Column(nullable = false)
    private boolean publicVisible = false;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Language> languages = new ArrayList<>();

    /**
     * Aktualisiert den Zeitstempel bei jedem Speichern und jeder Aenderung.
     * Grundlage fuer die Spalte "zuletzt aktualisiert" in der Berateruebersicht (F05).
     */
    @PrePersist
    @PreUpdate
    void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Fuegt eine Berufserfahrung hinzu und setzt die Rueckreferenz.
     *
     * @param entry der neue Eintrag
     */
    public void addWorkExperience(WorkExperience entry) {
        workExperiences.add(entry);
        entry.setCv(this);
    }

    /**
     * Entfernt eine Berufserfahrung. Durch {@code orphanRemoval} wird der
     * Eintrag beim naechsten Flush auch aus der Datenbank geloescht.
     *
     * @param entry der zu entfernende Eintrag
     */
    public void removeWorkExperience(WorkExperience entry) {
        workExperiences.remove(entry);
        entry.setCv(null);
    }

    /**
     * Fuegt eine Ausbildung hinzu und setzt die Rueckreferenz.
     *
     * @param entry der neue Eintrag
     */
    public void addEducation(Education entry) {
        educations.add(entry);
        entry.setCv(this);
    }

    /**
     * Entfernt eine Ausbildung samt Datenbankeintrag.
     *
     * @param entry der zu entfernende Eintrag
     */
    public void removeEducation(Education entry) {
        educations.remove(entry);
        entry.setCv(null);
    }

    /**
     * Fuegt eine Kenntnis hinzu und setzt die Rueckreferenz.
     *
     * @param entry der neue Eintrag
     */
    public void addSkill(Skill entry) {
        skills.add(entry);
        entry.setCv(this);
    }

    /**
     * Entfernt eine Kenntnis samt Datenbankeintrag.
     *
     * @param entry der zu entfernende Eintrag
     */
    public void removeSkill(Skill entry) {
        skills.remove(entry);
        entry.setCv(null);
    }

    /**
     * Fuegt eine Sprache hinzu und setzt die Rueckreferenz.
     *
     * @param entry der neue Eintrag
     */
    public void addLanguage(Language entry) {
        languages.add(entry);
        entry.setCv(this);
    }

    /**
     * Entfernt eine Sprache samt Datenbankeintrag.
     *
     * @param entry der zu entfernende Eintrag
     */
    public void removeLanguage(Language entry) {
        languages.remove(entry);
        entry.setCv(null);
    }
}
