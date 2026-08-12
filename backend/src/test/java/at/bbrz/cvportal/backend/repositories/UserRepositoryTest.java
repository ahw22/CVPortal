package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User newUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("argon2id$platzhalter");
        u.setRole(Role.TEILNEHMER);
        u.setActive(true);
        return u;
    }

    @Test
    void generatesUUIDv7AndSetsCreatedAt() {
        User saved = userRepository.saveAndFlush(newUser("Andreas Z", "andreas.zincke@ahwz.dev"));

        assertNotNull(saved.getId());
        assertEquals(7, saved.getId().version());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByUsernameReturnsSavedUser() {
        userRepository.saveAndFlush(newUser("Andreas", "andreas@test.at"));
        entityManager.clear();

        assertTrue(userRepository.findByUsername("Andreas").isPresent());
        assertTrue(userRepository.findByUsername("none").isEmpty());
    }

    @Test
    void findByUsernameIsCaseSensitive() {
        userRepository.saveAndFlush(newUser("Andreas", "andreas@test.at"));

        assertTrue(userRepository.findByUsername("Andreas").isPresent());
        assertTrue(userRepository.findByUsername("andreas").isEmpty());
    }

    @Test
    void duplicateUsernameShouldThrow() {
        userRepository.saveAndFlush(newUser("andreas", "andreas@test.at"));

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(newUser("andreas", "test@test.at")));
    }

    @Test
    void findByEmailReturnsSavedUser() {
        userRepository.saveAndFlush(newUser("Andreas", "andreas@test.at"));
        entityManager.clear();

        assertTrue(userRepository.findByEmail("andreas@test.at").isPresent());
        assertTrue(userRepository.findByEmail("none").isEmpty());
    }

    @Test
    void duplicateEmailShouldThrow() {
        userRepository.saveAndFlush(newUser("andreas", "andreas@test.at"));

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(newUser("test", "Andreas@test.at")));
    }

    @Test
    void existsByUsernameReturnsTrueExistsAndIsCaseSensitive() {
        userRepository.saveAndFlush(newUser("andreas", "andreas@test.at"));

        assertTrue(userRepository.existsByUsername("andreas"));
        assertFalse(userRepository.existsByUsername("Andreas"));
    }

    @Test
    void emailGetsNormalizedOnSave() {
        userRepository.saveAndFlush(newUser("andreas", "  ANDREAS@TEST.AT "));
        entityManager.clear();

        assertTrue(userRepository.existsByEmail("andreas@test.at"));
        User user = userRepository.findByEmail("andreas@test.at").orElseThrow();
        assertEquals("andreas@test.at", user.getEmail());
    }
}