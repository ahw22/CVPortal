package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LanguageForm {
    private String language;
    private String level;
}