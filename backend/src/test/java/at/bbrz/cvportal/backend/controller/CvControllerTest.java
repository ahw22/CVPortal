package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.*;
import at.bbrz.cvportal.backend.entities.LanguageLevel;
import at.bbrz.cvportal.backend.entities.SkillLevel;
import at.bbrz.cvportal.backend.exceptions.CvNotFoundException;
import at.bbrz.cvportal.backend.exceptions.CvNotPublicException;
import at.bbrz.cvportal.backend.services.CvService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CvController.class)
class CvControllerTest {

    private static final UUID USER_ID = UUID.fromString("01a03d9d-c67f-7e08-a036-4a4a926c70c8");
    private static final LocalDate BIRTH_DATE = LocalDate.of(1996, 1, 1);

    private static final String UPDATE_JSON = """
            {
                "firstName":"Andreas",
                "lastName":"Zincke",
                "jobTitle":"Applikationsentwickler",
                "phone":"+43 660 1234567",
                "address":"Musterweg 1",
                "birthDate":"1996-01-01",
                "summary":"Kurzprofil"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CvService cvService;

    @Test
    void getOwnCvReturns200AndTheFullPayload() throws Exception {
        when(cvService.getOwnCv(USER_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/cv/me").with(token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andreas"))
                .andExpect(jsonPath("$.jobTitle").value("Applikationsentwickler"))
                .andExpect(jsonPath("$.birthDate").value("1996-01-01"))
                .andExpect(jsonPath("$.completeness").value(60))
                .andExpect(jsonPath("$.publicVisible").value(false))
                .andExpect(jsonPath("$.workExperiences[0].company").value("BBRZ"))
                .andExpect(jsonPath("$.skills[0].level").value("FORTGESCHRITTEN"))
                .andExpect(jsonPath("$.educations").isEmpty());
    }

    @Test
    void getOwnCvUsesTheTokenSubjectAsUserId() throws Exception {
        when(cvService.getOwnCv(any(UUID.class))).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/cv/me").with(token()))
                .andExpect(status().isOk());

        verify(cvService).getOwnCv(USER_ID);
    }

    @Test
    void missingCvBecomes404() throws Exception {
        when(cvService.getOwnCv(USER_ID)).thenThrow(new CvNotFoundException("Kein Lebenslauf"));

        mockMvc.perform(get("/api/cv/me").with(token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturns200AndHandsTheBodyToTheService() throws Exception {
        when(cvService.updateOwnCv(eq(USER_ID), any(CvUpdateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/cv/me").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Zincke"));

        verify(cvService).updateOwnCv(USER_ID, new CvUpdateRequest("Andreas",
                "Zincke",
                "Applikationsentwickler",
                "+43 660 1234567",
                "Musterweg 1",
                BIRTH_DATE,
                "Kurzprofil"));

    }

    @Test
    void tooLongFirstNameIsRejectedBEforeTheService() throws Exception {
        String tooLong = "A".repeat(51);

        mockMvc.perform(put("/api/cv/me").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName":"%s"
                                }
                                """.formatted(tooLong)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cvService);
    }

    @Test
    void futureBirthDateIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(put("/api/cv/me").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "birthDate":"2099-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cvService);
    }

    @Test
    void lettersInThePhoneNumberAreRejectedBeforeTheService() throws Exception {
        mockMvc.perform(put("/api/cv/me").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone":"kein Telefon"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cvService);
    }

    @Test
    void emptyBodyIsAcceptedAndReachesTheService() throws Exception {
        when(cvService.updateOwnCv(eq(USER_ID), any(CvUpdateRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/cv/me").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(cvService).updateOwnCv(USER_ID,
                new CvUpdateRequest(null, null, null, null, null, null, null));
    }

    @Test
    void visibilityToggleReturns200AndPassesTheFlag() throws Exception {
        when(cvService.updateVisibility(USER_ID, true)).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/cv/me/visibility").with(token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "publicVisible":true
                        }
                        """))
                .andExpect(status().isOk());

        verify(cvService).updateVisibility(USER_ID, true);
    }

    @Test
    void visibilityWithoutTheFlagIsBadRequest() throws Exception {
        mockMvc.perform(put("/api/cv/me/visibility").with(token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cvService);
    }

    @Test
    void publicCvReturns200WithoutAToken() throws Exception {
        when(cvService.getPublicCv("andreas")).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/cv/public/andreas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andreas"));
    }

    @Test
    void privateCvBecomes403() throws  Exception {
        when(cvService.getPublicCv("andreas")).thenThrow(new CvNotPublicException());

        mockMvc.perform(get("/api/cv/public/andreas"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownPublicUsernameBecomes404() throws Exception {
        when(cvService.getPublicCv("gibt-es-nicht")).thenThrow(new CvNotFoundException("Kein Lebenslauf"));

        mockMvc.perform(get("/api/cv/public/gibt-es-nicht"))
                .andExpect(status().isNotFound());
    }

    private RequestPostProcessor token() {
        return jwt().jwt(builder -> builder
                .subject(USER_ID.toString())
                .claim("username", "andreas")
                .claim("role", "TEILNEHMER"));

    }

    private CvResponse sampleResponse() {
        return new CvResponse(
                "andreas",
                "andreas@test.at",
                "Andreas",
                "Zincke",
                "Applikationsentwickler",
                "+43 660 1234567",
                "Musterweg 1",
                BIRTH_DATE,
                "Kurzprofil",
                false,
                60,
                LocalDateTime.of(2026, 8, 26, 10, 0),
                List.of(new WorkExperienceResponse(1L,
                        "BBRZ",
                        "Entwickler",
                        LocalDate.of(2024, 1, 1),
                        null,
                        "Beschreibung",
                        0)),
                List.of(),
                List.of(new SkillResponse(2L, "Java", SkillLevel.FORTGESCHRITTEN)),
                List.of(new LanguageResponse(3L, "Englisch", LanguageLevel.B2))
        );
    }

    @TestConfiguration
    static class PermitAllSecurityConfig {

        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }


}