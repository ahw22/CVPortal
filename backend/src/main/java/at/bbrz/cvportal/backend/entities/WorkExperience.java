package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Einzelne Station der Berufserfahrung innerhalb eines {@link CurriculumVitae}.
 */
@Entity
@Table(name = "work_experience")
@Getter
@Setter
@NoArgsConstructor
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_work_experience_cv"))
    private CurriculumVitae cv;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * {@code null} bedeutet fachlich "bis heute".
     */
    private LocalDate endDate;

    @Column(length = 2000)
    private String description;

    /**
     * Anzeigereihenfolge im Lebenslauf, kleinster Wert zuerst.
     */
    @Column(nullable = false)
    private int sortOrder;
}
