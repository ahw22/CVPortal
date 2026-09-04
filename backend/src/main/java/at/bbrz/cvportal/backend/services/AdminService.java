package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.ParticipantResponse;
import at.bbrz.cvportal.backend.dtos.UserResponse;
import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.exceptions.UserNotFoundException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import at.bbrz.cvportal.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CurriculumVitaeRepository cvRepository;
    private final CvService cvService;
    private final CvMapper mapper;

    @Transactional(readOnly = true)
    public List<ParticipantResponse> findAllParticipants() {
        return cvRepository.findByUserRoleOrderByUserUsernameAsc(Role.TEILNEHMER).stream()
                .map(cv -> mapper.toResponse(cv, cvService.calculateCompleteness(cv)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse setActive(UUID adminId, UUID userId, boolean active) {
        User user = loadOther(adminId, userId);
        user.setActive(active);
        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse setRole(UUID adminId, UUID userId, Role role) {
        User user = loadOther(adminId, userId);
        user.setRole(role);
        return mapper.toResponse(user);
    }

    private User loadOther(UUID adminId, UUID userId) {
        if (adminId.equals(userId)) {
            throw new AccessDeniedException("Das eigene Konto kann nicht geändert werden");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Benutzer nicht gefunden"));
    }

}
