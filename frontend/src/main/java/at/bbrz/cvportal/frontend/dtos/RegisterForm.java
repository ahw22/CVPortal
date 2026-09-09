package at.bbrz.cvportal.frontend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterForm {

    @NotBlank(message = "Bitte geben Sie einen Benutzernamen ein.")
    @Size(min = 3, max = 50, message = "Der Benutzername muss zwischen 3 und 50 Zeichen lang sein")
    @Pattern(regexp = "[A-Za-z0-9-]+",
            message = "Nur Buchstaben, Ziffern und Bindestrich erlaubt.")
    private String username;

    @NotBlank(message = "Bitte geben Sie eine E-Mail-Adresse ein.")
    @Email(message = "Bitte geben Sie eine gültige E-Mail-Adresse ein.")
    @Size(max = 150, message = "Die E-Mail-Adresse darf höchstens 150 Zeichen lang sein.")
    private String email;

    @NotBlank(message = "Bitte geben Sie ein Passwort ein.")
    @Size(min = 8, max = 100, message = "Das Passwort muss mindestens 8 Zeichen lang sein.")
    private String password;

    @Override
    public String toString() {
        return "RegisterForm{username='" + username + "', email='" + email + "', password='****'}";
    }
}
