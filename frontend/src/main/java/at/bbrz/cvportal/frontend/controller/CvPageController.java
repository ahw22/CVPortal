package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.config.BackendProperties;
import at.bbrz.cvportal.frontend.dtos.CvForm;
import at.bbrz.cvportal.frontend.dtos.CvResponse;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import at.bbrz.cvportal.frontend.security.ApiUser;
import at.bbrz.cvportal.frontend.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Die Bearbeitungsseite des eigenen Lebenslaufs (F02).
 * <p>
 * Die Stammdaten laufen wie alles andere serverseitig über den ApiClientService. Der
 * Sichtbarkeits-Toggle ist die einzige Ausnahme im Projekt: der geht
 * direkt per {@code fetch()} aus dem Browser ans Backend und löst damit den einzigen
 * echten CORS-Preflight aus (F07).
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CvPageController {

    private static final String VIEW_EDIT = "cv/edit";
    private static final String VIEW_DASHBOARD = "cv/dashboard";
    private static final String REDIRECT_EDIT = "redirect:/cv/edit";

    private static final String ATTR_FORM = "cvForm";
    private static final String ATTR_CV = "cv";
    private static final String ATTR_VISIBILITY_URL = "visibilityUrl";
    private static final String ATTR_SUCCESS = "erfolg";
    private static final String ATTR_ERROR = "fehler";
    private static final String ATTR_JWT = "jwt";

    private final ApiClientService apiClientService;
    private final BackendProperties backendProperties;


    /**
     * Leere Eingabefelder kommen als "" an. Ohne das hier würde ein leeres Feld als leerer
     * String statt als {@code null} ans Backend gehen und in die Vollständigkeitsberechnung
     * einfliessen.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/cv/edit")
    public String edit(@AuthenticationPrincipal ApiUser user, Model model) {
        CvResponse cv = apiClientService.getOwnCv(user.token());

        model.addAttribute(ATTR_CV, cv);
        model.addAttribute(ATTR_FORM, CvForm.von(cv));
        model.addAttribute(ATTR_VISIBILITY_URL,
                backendProperties.baseUrl() + "/api/cv/me/visibility");
        // Das JWT verlässt hier bewusst den Server. Der Sichtbarkeits-Toggle ruft das
        // Backend direkt aus dem Browser auf und braucht den Bearer-Header dafür (F07).
        model.addAttribute(ATTR_JWT, user.token());
        return VIEW_EDIT;
    }

    @PostMapping("/cv/edit")
    public String save(@ModelAttribute(ATTR_FORM) CvForm form,
                       @AuthenticationPrincipal ApiUser user,
                       RedirectAttributes flash) {
        try {
            apiClientService.updateOwnCv(form, user.token());
            flash.addFlashAttribute(ATTR_SUCCESS, "Lebenslauf gespeichert.");
        } catch (ApiException e) {
            log.warn("Speichern fehlgeschlagen: {}", e.getMessage());
            flash.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_EDIT;
    }


    /**
     * Einstiegsseite nach dem Login. Nutzt denselben Aufruf wie die Bearbeitungsseite.
     * {@code CvResponse} trägt Vollständigkeit und Sichtbarkeit bereits mit.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal ApiUser user, Model model) {
        model.addAttribute(ATTR_CV, apiClientService.getOwnCv(user.token()));
        return VIEW_DASHBOARD;
    }
}