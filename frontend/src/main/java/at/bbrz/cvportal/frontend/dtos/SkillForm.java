package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code level} ist der Enum-Name, z.B. FORTGESCHRITTEN - Jackson wandelt ihn im Backend.
 */
@Getter
@Setter
@NoArgsConstructor
public class SkillForm {
    private String name;
    private String level;
}