package at.bbrz.cvportal.frontend.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterForm {

    private String username;
    private String email;
    private String password;

    @Override
    public String toString() {
        return "RegisterForm{username='" + username + "', email='" + email + "', password='****'}";
    }
}
