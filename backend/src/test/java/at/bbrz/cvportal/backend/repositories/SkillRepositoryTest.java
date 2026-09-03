package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SkillRepositoryTest {

    @Autowired
    private SkillRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdAndUserIdFindsTheOwnEntry() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        Skill entry = newEntry(owner.getCurriculumVitae(), "Java", 0);
        entityManager.persistAndFlush(owner);
        entityManager.clear();

        assertTrue(repository.findByIdAndCv_User_Id(entry.getId(), owner.getId()).isPresent());
    }

    @Test
    void findByIdAndUserIdHidesAForeignEntry() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        Skill entry = newEntry(owner.getCurriculumVitae(), "Java", 0);
        User other = newUserWithCv("max", "mustermann@test.at");
        entityManager.persist(owner);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.findById(entry.getId()).isPresent());
        assertTrue(repository.findByIdAndCv_User_Id(entry.getId(), other.getId()).isEmpty());
    }

    @Test
    void listIsScopedToTheOwnerAndSortedBySortOrder() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        newEntry(owner.getCurriculumVitae(), "Zweite", 1);
        newEntry(owner.getCurriculumVitae(), "Erste", 0);
        User other = newUserWithCv("max", "mustermann@test.at");
        newEntry(other.getCurriculumVitae(), "Fremd", 0);
        entityManager.persist(owner);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        List<Skill> entries = repository.findByCv_User_IdOrderBySortOrderAsc(owner.getId());

        assertEquals(2, entries.size());
        assertEquals("Erste", entries.get(0).getName());
        assertEquals("Zweite", entries.get(1).getName());
    }

    @Test
    void findFirstDescReturnsTheHighestSortOrder() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        newEntry(owner.getCurriculumVitae(), "Erste", 0);
        newEntry(owner.getCurriculumVitae(), "Zweite", 1);
        entityManager.persistAndFlush(owner);
        entityManager.clear();

        Skill highest = repository
                .findFirstByCv_User_IdOrderBySortOrderDesc(owner.getId())
                .orElseThrow();

        assertEquals(1, highest.getSortOrder());
    }

    @Test
    void findFirstDescIsEmptyForUserWithoutEntries() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        entityManager.persistAndFlush(owner);
        entityManager.clear();

        assertTrue(repository.findFirstByCv_User_IdOrderBySortOrderDesc(owner.getId()).isEmpty());
    }

    private User newUserWithCv(String username, String mail) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(mail);
        user.setPassword("$argon2id$platzhalter");
        user.setRole(Role.TEILNEHMER);
        user.setActive(true);
        user.setCurriculumVitae(new CurriculumVitae());
        return user;
    }

    private Skill newEntry(CurriculumVitae cv, String skillName, int sortOrder) {
        Skill entry = new Skill();
        entry.setName(skillName);
        entry.setLevel(SkillLevel.FORTGESCHRITTEN);
        entry.setSortOrder(sortOrder);
        cv.addSkill(entry);
        return entry;
    }
}