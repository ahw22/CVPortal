package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.CardResponse;
import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CurriculumVitaeRepository repository;

    @Transactional(readOnly = true)
    public CardResponse getCard(String username) {
        CurriculumVitae cv = repository.findByUserUsername(username)
                .orElseThrow(() -> new CvNotFoundException("Keine Visitenkarte für " + username));

        if (!cv.getUser().isActive()) {
            throw new CvNotFoundException("Keine Visitenkarte für " + username);
        }

        return new CardResponse(
                cv.getUser().getUsername(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getJobTitle(),
                cv.getSummary(),
                cv.getUser().getEmail(),
                cv.getPhone(),
                cv.getProfilePhotoBase64(),
                cv.isPublicVisible());
    }
}
