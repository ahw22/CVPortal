package at.bbrz.cvportal.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fachliche Kenntnis mit Selbsteinschaetzung innerhalb eines {@link CurriculumVitae}.
 */
@Entity
@Table(name = "skill")
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cv_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_skill_cv"))
    private CurriculumVitae cv;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillLevel level;

    @Column(nullable = false)
    private int sortOrder;
}
