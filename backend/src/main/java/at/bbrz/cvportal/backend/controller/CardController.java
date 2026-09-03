package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.CardResponse;
import at.bbrz.cvportal.backend.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService service;

    @GetMapping("/{username}")
    public CardResponse getCard(@PathVariable String username) {
        return service.getCard(username);
    }
}
