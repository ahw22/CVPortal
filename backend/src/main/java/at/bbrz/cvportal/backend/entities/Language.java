package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sprachkenntnis mit Niveau nach GER innerhalb eines {@link CurriculumVitae}.
 */
@Entity
@Table(name = "cv_language", uniqueConstraints =
        @UniqueConstraint(name = "uk_language_cv_name", columnNames = {"cv_id", "language_name"}))
@Getter
@Setter
@NoArgsConstructor
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_language_cv"))
    private CurriculumVitae cv;

    /**
     * Spaltenname bewusst {@code language_name}: LANGUAGE ist ein reserviertes
     * Wort im SQL-Standard.
     */
    @Column(name = "language_name", nullable = false, length = 60)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LanguageLevel level;

    @Column(nullable = false)
    private int sortOrder;
}
