package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.exceptions.ApiException;
import at.bbrz.cvportal.frontend.security.ApiUser;
import at.bbrz.cvportal.frontend.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private static final String VIEW_PARTICIPANTS = "admin/participants";
    private static final String VIEW_USERS = "admin/users";
    private static final String REDIRECT_USERS = "redirect:/admin/users";
    private static final String ATTR_ERROR = "fehler";

    private final ApiClientService apiClientService;

    @GetMapping("/participants")
    public String participants(@AuthenticationPrincipal ApiUser user, Model model) {
        model.addAttribute("participants", apiClientService.findParticipants(user.token()));
        return VIEW_PARTICIPANTS;
    }

    @GetMapping("/users")
    private String users(@AuthenticationPrincipal ApiUser user, Model model) {
        model.addAttribute("users", apiClientService.findUsers(user.token()));
        return VIEW_USERS;
    }

    @PostMapping("/users/{id}/active")
    public String setActive(@PathVariable String id,
                            @RequestParam boolean active,
                            @AuthenticationPrincipal ApiUser user,
                            RedirectAttributes flash) {
        try {
            apiClientService.setActive(id, active, user.token());
        } catch (ApiException e) {
            log.warn("setActive fehlgeschlagen für {}: {}", id, e.getMessage());
            flash.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/role")
    public String setRole(@PathVariable String id,
                          @RequestParam String role,
                          @AuthenticationPrincipal ApiUser user,
                          RedirectAttributes flash) {
        try {
            apiClientService.setRole(id, role, user.token());
        } catch (ApiException e) {
            log.warn("setRole fehlgeschlagen für {}: {}", id, e.getMessage());
            flash.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_USERS;
    }
}
