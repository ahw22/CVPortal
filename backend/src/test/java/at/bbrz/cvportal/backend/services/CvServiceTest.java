package at.bbrz.cvportal.backend.services;

import at.bbrz.cvportal.backend.dtos.CvResponse;
import at.bbrz.cvportal.backend.dtos.CvUpdateRequest;
import at.bbrz.cvportal.backend.entities.*;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.CvNotPublicException;
import at.bbrz.cvportal.backend.repositories.CurriculumVitaeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CvServiceTest {

    private static final UUID USER_ID = UUID.fromString("01a03d9d-c67f-7e08-a036-4a4a926c70c8");
    private static final LocalDate BIRTH_DATE = LocalDate.of(1996, 1, 1);

    @Mock
    private CurriculumVitaeRepository repository;

    @Spy
    private CvMapper mapper = new CvMapper();

    @InjectMocks
    private CvService service;

    @Test
    void emptyCvIsZeroPercent() {
        assertEquals(0, service.calculateCompleteness(emptyCv()));
    }

    @Test
    void fullCvIsHundredPercent() {
        assertEquals(100, service.calculateCompleteness(fullCv()));
    }

    @Test
    void everyCriterionIsWorthTenPercent() {
        CurriculumVitae cv = emptyCv();

        cv.setFirstName("Andreas");
        assertEquals(10, service.calculateCompleteness(cv));

        cv.setLastName("Zincke");
        assertEquals(20, service.calculateCompleteness(cv));

        cv.addSkill(new Skill());
        assertEquals(30, service.calculateCompleteness(cv));
    }

    @Test
    void stammdatenAloneAreSixtyPercent() {
        assertEquals(60, service.calculateCompleteness(cvWithSomeData()));
    }

    @Test
    void blankFieldsDoNotCount() {
        CurriculumVitae cv = emptyCv();
        cv.setFirstName("     ");
        cv.setLastName("");

        assertEquals(0, service.calculateCompleteness(cv));
    }

    @Test
    void getOwnCvMapsStammdatenAndUserFields() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cvWithSomeData()));

        CvResponse ownCv = service.getOwnCv(USER_ID);

        assertEquals("andreas", ownCv.username());
        assertEquals("andreas@test.at", ownCv.email());
        assertEquals("Andreas", ownCv.firstName());
        assertEquals("Applikationsentwickler", ownCv.jobTitle());
        assertEquals(BIRTH_DATE, ownCv.birthDate());
        assertEquals(60, ownCv.completeness());
    }

    @Test
    void getOwnCvMapsTheChildCollections() {
        CurriculumVitae cv = emptyCv();
        WorkExperience job = new WorkExperience();
        job.setCompany("BBRZ");
        job.setPosition("Entwickler");
        job.setStartDate(LocalDate.of(2024, 1, 1));
        job.setSortOrder(0);
        cv.addWorkExperience(job);

        Skill skill = new Skill();
        skill.setName("Java");
        skill.setLevel(SkillLevel.FORTGESCHRITTEN);
        cv.addSkill(skill);

        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        CvResponse ownCv = service.getOwnCv(USER_ID);

        assertEquals(1, ownCv.workExperiences().size());
        assertEquals("BBRZ", ownCv.workExperiences().getFirst().company());
        assertEquals(1, ownCv.skills().size());
        assertEquals("Java", ownCv.skills().getFirst().name());
        assertEquals(SkillLevel.FORTGESCHRITTEN, ownCv.skills().getFirst().level());
        assertTrue(ownCv.educations().isEmpty());
        assertTrue(ownCv.languages().isEmpty());
    }

    @Test
    void getOwnCvForUnknownUserThrows() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(CvNotFoundException.class, () -> service.getOwnCv(USER_ID));
    }

    @Test
    void updateWritesTheDataOntoTheEntity() {
        CurriculumVitae cv = emptyCv();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        service.updateOwnCv(USER_ID, new CvUpdateRequest(
                "Andreas",
                "Zincke",
                "Applikationsentwickler",
                "+46 660 1234567",
                "Musterweg 1",
                BIRTH_DATE,
                "Kurzprofil"));

        assertEquals("Andreas", cv.getFirstName());
        assertEquals("Applikationsentwickler", cv.getJobTitle());
        assertEquals(BIRTH_DATE, cv.getBirthDate());
    }

    @Test
    void updateReturnsTheNewStateSoTheFrontendNeedsNoSecondGet() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCv()));

        CvResponse response = service.updateOwnCv(USER_ID,
                new CvUpdateRequest("Andreas",
                        "Zincke",
                        "Applikationsentwickler",
                        "+43 660 1234567",
                        "Musterweg 1",
                        BIRTH_DATE,
                        "Kurzprofil"));

        assertEquals("Zincke", response.lastName());
        assertEquals(60, response.completeness());
    }

    @Test
    void updateTrimsAndTurnsBlankIntoNull() {
        CurriculumVitae cv = emptyCv();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        service.updateOwnCv(USER_ID,
                new CvUpdateRequest("  Andreas   ",
                        "    ",
                        null,
                        null,
                        null,
                        null,
                        null));

        assertEquals("Andreas", cv.getFirstName());
        assertNull(cv.getLastName());
    }

    @Test
    void emptyRequestClearsAllData() {
        CurriculumVitae cv = cvWithSomeData();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        CvResponse response = service.updateOwnCv(USER_ID, new CvUpdateRequest(null,
                null,
                null,
                null,
                null,
                null,
                null));

        assertNull(cv.getFirstName());
        assertNull(cv.getJobTitle());
        assertEquals(0, response.completeness());
    }

    @Test
    void updateDoesNotTouchTheVisibility() {
        CurriculumVitae cv = emptyCv();
        cv.setPublicVisible(true);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        CvResponse response = service.updateOwnCv(USER_ID,
                new CvUpdateRequest("Andreas", null, null, null, null, null, null));

        assertTrue(response.publicVisible());
    }

    @Test
    void visibilityCanBeSwitchedOnAndOff() {
        CurriculumVitae cv = emptyCv();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(cv));

        assertTrue(service.updateVisibility(USER_ID, true).publicVisible());
        assertTrue(cv.isPublicVisible());

        assertFalse(service.updateVisibility(USER_ID, false).publicVisible());
        assertFalse(cv.isPublicVisible());
    }

    @Test
    void publicCvIsReturnedWhenReleased() {
        CurriculumVitae cv = cvWithSomeData();
        cv.setPublicVisible(true);
        when(repository.findByUserUsername("andreas")).thenReturn(Optional.of(cv));

        CvResponse response = service.getPublicCv("andreas");

        assertEquals("andreas", response.username());
        assertTrue(response.publicVisible());
    }

    @Test
    void privateCvIsRefused() {
        CurriculumVitae cv = cvWithSomeData();
        cv.setPublicVisible(false);
        when(repository.findByUserUsername("andreas")).thenReturn(Optional.of(cv));

        assertThrows(CvNotPublicException.class, () -> service.getPublicCv("andreas"));
    }

    @Test
    void deactivatedUserIsNotDeliveredEvenWhenPublic() {
        CurriculumVitae cv = new CurriculumVitae();
        cv.setUser(newUser("gesperrt", false));
        cv.setPublicVisible(true);
        when(repository.findByUserUsername("gesperrt")).thenReturn(Optional.of(cv));

        assertThrows(CvNotFoundException.class, () -> service.getPublicCv("gesperrt"));
    }

    @Test
    void unknownUsernameThrows() {
        when(repository.findByUserUsername("gibt-es-nicht")).thenReturn(Optional.empty());

        assertThrows(CvNotFoundException.class, () -> service.getPublicCv("gibt-es-nicht"));
    }


    private User newUser(String username, boolean active) {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(username);
        user.setEmail(username + "@test.at");
        user.setPassword("$argon2id$platzhalter");
        user.setRole(Role.TEILNEHMER);
        user.setActive(active);
        return user;
    }

    private CurriculumVitae emptyCv() {
        CurriculumVitae cv = new CurriculumVitae();
        cv.setUser(newUser("andreas", true));
        return cv;
    }

    private CurriculumVitae cvWithSomeData() {
        CurriculumVitae cv = emptyCv();
        cv.setFirstName("Andreas");
        cv.setLastName("Zincke");
        cv.setJobTitle("Applikationsentwickler");
        cv.setPhone("+43 660 1234567");
        cv.setAddress("Musterweg 1, 8010 Graz");
        cv.setBirthDate(BIRTH_DATE);
        cv.setSummary("Kurzprofil");
        return cv;
    }

    private CurriculumVitae fullCv() {
        CurriculumVitae cv = cvWithSomeData();
        cv.addWorkExperience(new WorkExperience());
        cv.addEducation(new Education());
        cv.addSkill(new Skill());
        cv.addLanguage(new Language());
        return cv;
    }
}