package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.dtos.RegisterForm;
import at.bbrz.cvportal.frontend.dtos.UserResponse;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import at.bbrz.cvportal.frontend.service.ApiClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private static final String VIEW_LOGIN = "auth/login";
    private static final String VIEW_REGISTER = "auth/register";

    private static final String REDIRECT_DASHBOARD = "redirect:/dashboard";
    private static final String REDIRECT_LOGIN_REGISTERED = "redirect:/login?registered";

    private static final String ATTR_FORM = "registerForm";
    private static final String ERROR_CODE_BACKEND = "backend";

    private final ApiClientService apiClientService;

    // Root hat keine eigene Seite. Eingeloggte user sollen aufs Dashboard.
    @GetMapping("/")
    public String index() {
        return REDIRECT_DASHBOARD;
    }

    @GetMapping("/login")
    public String login() {
        return VIEW_LOGIN;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute(ATTR_FORM, new RegisterForm());
        return VIEW_REGISTER;
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute(ATTR_FORM) RegisterForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VIEW_REGISTER;
        }

        try {
            UserResponse created = apiClientService.register(form);
            log.info("Neuer Benutzer registriert: {}", created.username());
            return REDIRECT_LOGIN_REGISTERED;
        } catch (ApiException e) {
            handleErrors(e, bindingResult);
            return VIEW_REGISTER;
        }
    }

    //TODO: For testing remove later

    @GetMapping("/dashboard")
    public String dashboard() {
        return "cv/dashboard";
    }

    /**
     * Mapped die Meldungen des Backends auf das Formular.
     * <p>
     * Bei HTTP 400 liefert das Backend eine Feldliste, die dann unter den betroffenen Eingabefeldern landet.
     * Alles andere wie z.B. Backend nicht erreichbar wird zur Meldung über dem Formular
     * {@code rejectValue wirft wenn das Backend ein Feld meldet das es im Formular nicht gibt.}
     */
    private void handleErrors(ApiException e, BindingResult bindingResult) {
        if (!e.hasFieldErrors()) {
            bindingResult.reject(ERROR_CODE_BACKEND, e.getMessage());
            return;
        }

        e.getFieldErrors().forEach((field, message) -> {
            try {
                bindingResult.rejectValue(field, ERROR_CODE_BACKEND, message);
            } catch (NotReadablePropertyException ex) {
                log.warn("Backend meldet unbekanntes Feld '{}'", field);
                bindingResult.reject(ERROR_CODE_BACKEND, message);
            }
        });
    }
}
