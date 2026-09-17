package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.dtos.CardResponse;
import at.bbrz.cvportal.frontend.dtos.CvResponse;
import at.bbrz.cvportal.frontend.exceptions.ApiException;
import at.bbrz.cvportal.frontend.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Die beiden öffentlichen Ansichten ohne Anmeldung erreichbar (F03, F04).
 * <p>
 * Die Visitenkarte ist immer sichtbar der Lebenslauf nur wenn er freigegeben ist. Das
 * Backend antwortet auf einen gesperrten Lebenslauf mit 403. Daraus wird hier eine eigene
 * Seite statt einer Fehlermeldung. Der Besucher soll nicht erfahren müssen, ob überhaupt
 * ein Lebenslauf existiert (Testfall 7).
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PublicPageController {

    private static final String VIEW_CARD = "card/card";
    private static final String VIEW_CV = "cv/public";
    private static final String VIEW_CV_PRIVATE = "cv/private";
    private static final String VIEW_NOT_FOUND = "error/404";

    private final ApiClientService apiClientService;

    @GetMapping("/card/{username}")
    public String card(@PathVariable String username, Model model) {
        try {
            CardResponse card = apiClientService.getCard(username);
            model.addAttribute("card", card);
            return VIEW_CARD;
        } catch (ApiException e) {
            if (e.is(HttpStatus.NOT_FOUND)) {
                model.addAttribute("username", username);
                return VIEW_NOT_FOUND;
            }
            throw e;
        }
    }

    @GetMapping("/cv/{username}")
    public String publicCv(@PathVariable String username, Model model) {
        model.addAttribute("username", username);
        try {
            CvResponse cv = apiClientService.getPublicCv(username);
            model.addAttribute("cv", cv);
            return VIEW_CV;
        } catch (ApiException e) {
            if (e.is(HttpStatus.FORBIDDEN)) {
                return VIEW_CV_PRIVATE;
            }
            if (e.is(HttpStatus.NOT_FOUND)) {
                return VIEW_NOT_FOUND;
            }
            throw e;
        }
    }
}