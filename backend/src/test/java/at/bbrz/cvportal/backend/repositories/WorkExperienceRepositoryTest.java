package at.bbrz.cvportal.backend.repositories;

import at.bbrz.cvportal.backend.entities.CurriculumVitae;
import at.bbrz.cvportal.backend.entities.Role;
import at.bbrz.cvportal.backend.entities.User;
import at.bbrz.cvportal.backend.entities.WorkExperience;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WorkExperienceRepositoryTest {

    @Autowired
    private WorkExperienceRepository repository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void findByIdAndUserIdFindsTheOwnEntry() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        WorkExperience entry = newEntry(owner.getCurriculumVitae(), "BBRZ", 0);
        entityManager.persistAndFlush(owner);
        entityManager.clear();

        assertTrue(repository.findByIdAndCv_User_Id(entry.getId(), owner.getId()).isPresent());
    }

    @Test
    void findByIdAndUserIdHidesAForeignEntry() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        WorkExperience entry = newEntry(owner.getCurriculumVitae(), "BBRZ", 0);
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
        newEntry(owner.getCurriculumVitae(),"Erste", 0);
        User other = newUserWithCv("max", "mustermann@test.at");
        newEntry(other.getCurriculumVitae(), "Fremd", 0);
        entityManager.persist(owner);
        entityManager.persist(other);
        entityManager.flush();
        entityManager.clear();

        List<WorkExperience> entries = repository.findByCv_User_IdOrderBySortOrderAsc(owner.getId());

        assertEquals(2, entries.size());
        assertEquals("Erste", entries.get(0).getCompany());
        assertEquals("Zweite", entries.get(1).getCompany());
    }

    @Test
    void findFirstDescReturnsTheHighestSortOrder() {
        User owner = newUserWithCv("andreas", "andreas@test.at");
        newEntry(owner.getCurriculumVitae(), "Erste", 0);
        newEntry(owner.getCurriculumVitae(), "Zweite", 1);
        entityManager.persistAndFlush(owner);
        entityManager.clear();

        WorkExperience highest = repository
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

    private WorkExperience newEntry(CurriculumVitae cv, String company, int sortOrder) {
        WorkExperience entry = new WorkExperience();
        entry.setCompany(company);
        entry.setPosition("Applikationsentwickler");
        entry.setStartDate(LocalDate.of(2024,1,1));
        entry.setSortOrder(sortOrder);
        cv.addWorkExperience(entry);
        return entry;
    }
}