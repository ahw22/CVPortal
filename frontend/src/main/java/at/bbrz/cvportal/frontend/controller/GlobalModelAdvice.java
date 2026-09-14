package at.bbrz.cvportal.frontend.controller;

import at.bbrz.cvportal.frontend.security.ApiUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public ApiUser currentUser(@AuthenticationPrincipal ApiUser user) {
        return user;
    }
}
