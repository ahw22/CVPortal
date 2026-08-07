package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Einzelne Ausbildungsstation innerhalb eines {@link CurriculumVitae}.
 */
@Entity
@Table(name = "education")
@Getter
@Setter
@NoArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_education_cv"))
    private CurriculumVitae cv;

    @Column(nullable = false, length = 150)
    private String institution;

    @Column(nullable = false, length = 100)
    private String degree;

    @Column(length = 100)
    private String fieldOfStudy;

    @Column(nullable = false)
    private LocalDate startDate;

    /** {@code null} bedeutet fachlich "laufend". */
    private LocalDate endDate;

    /** Anzeigereihenfolge im Lebenslauf, kleinster Wert zuerst. */
    @Column(nullable = false)
    private int sortOrder;
}
