package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.config.BackendProperties;
import at.bbrz.cvportal.frontend.dtos.*;
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
    private static final String PATH_WORK = "/api/cv/me/work-experience";
    private static final String PATH_SKILLS = "/api/cv/me/skills";
    private static final String PATH_LANGUAGES = "/api/cv/me/languages";
    private static final String PATH_EDUCATION = "/api/cv/me/education";

    private static final String MSG_ADDED = "Eintrag hinzugefügt.";
    private static final String MSG_DELETED = "Eintrag gelöscht.";

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

    /**
     * Einstiegsseite nach dem Login. Nutzt denselben Aufruf wie die Bearbeitungsseite.
     * {@code CvResponse} trägt Vollständigkeit und Sichtbarkeit bereits mit.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal ApiUser user, Model model) {
        model.addAttribute(ATTR_CV, apiClientService.getOwnCv(user.token()));
        return VIEW_DASHBOARD;
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

    @PostMapping("/cv/edit/work-experience")
    public String addWork(@ModelAttribute WorkExperienceForm form,
                          @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return add(PATH_WORK, form, user, flash);
    }

    @PostMapping("/cv/edit/work-experience/{id}/delete")
    public String deleteWork(@PathVariable Long id,
                             @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return remove(PATH_WORK, id, user, flash);
    }

    @PostMapping("/cv/edit/skills")
    public String addSkill(@ModelAttribute SkillForm form,
                           @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return add(PATH_SKILLS, form, user, flash);
    }

    @PostMapping("/cv/edit/skills/{id}/delete")
    public String deleteSkill(@PathVariable Long id,
                              @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return remove(PATH_SKILLS, id, user, flash);
    }

    @PostMapping("/cv/edit/languages")
    public String addLanguage(@ModelAttribute LanguageForm form,
                              @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return add(PATH_LANGUAGES, form, user, flash);
    }

    @PostMapping("/cv/edit/languages/{id}/delete")
    public String deleteLanguage(@PathVariable Long id,
                                 @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return remove(PATH_LANGUAGES, id, user, flash);
    }

    @PostMapping("/cv/edit/education")
    public String addEducation(@ModelAttribute EducationForm form,
                               @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return add(PATH_EDUCATION, form, user, flash);
    }

    @PostMapping("/cv/edit/education/{id}/delete")
    public String deleteEducation(@PathVariable Long id,
                                  @AuthenticationPrincipal ApiUser user, RedirectAttributes flash) {
        return remove(PATH_EDUCATION, id, user, flash);
    }

    /**
     * Geprüft wird im Backend. Schlaegt das fehl, landet die deutsche Meldung als
     * Alert über dem Formular.
     */
    private String add(String path, Object form, ApiUser user, RedirectAttributes flash) {
        try {
            apiClientService.addEntry(path, form, user.token());
            flash.addFlashAttribute(ATTR_SUCCESS, MSG_ADDED);
        } catch (ApiException e) {
            log.warn("Anlegen fehlgeschlagen ({}): {}", path, e.getMessage());
            flash.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_EDIT;
    }

    private String remove(String path, Long id, ApiUser user, RedirectAttributes flash) {
        try {
            apiClientService.deleteEntry(path, id, user.token());
            flash.addFlashAttribute(ATTR_SUCCESS, MSG_DELETED);
        } catch (ApiException e) {
            log.warn("Loeschen fehlgeschlagen ({}/{}): {}", path, id, e.getMessage());
            flash.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_EDIT;
    }
}