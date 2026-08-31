package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.WorkExperienceRequest;
import at.bbrz.cvportal.backend.dtos.WorkExperienceResponse;
import at.bbrz.cvportal.backend.exceptions.EntryNotFoundException;
import at.bbrz.cvportal.backend.services.WorkExperienceService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkExperienceController.class)
class WorkExperienceControllerTest {

    private static final UUID USER_ID = UUID.fromString("01a0385f-b91c-7434-937d-930f09ab3001");
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);

    private static final String CREATE_JSON = """
            {
                "company":"BBRZ",
                "position":"Applikationsentwickler",
                "startDate":"2024-01-01",
                "description":"Beschreibung"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkExperienceService service;

    @Test
    void findAllReturns200AndTheEntries() throws Exception {
        when(service.findAll(USER_ID)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/cv/me/work-experience").with(token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].company").value("BBRZ"))
                .andExpect(jsonPath("$[0].endDate").isEmpty());
    }

    @Test
    void findAllUsesTheTokenSubjectAsUserId() throws Exception {
        when(service.findAll(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/cv/me/work-experience").with(token()))
                .andExpect(status().isOk());

        verify(service).findAll(USER_ID);
    }

    @Test
    void createReturns201AndHandsTheBodyToTheService() throws Exception {
        when(service.create(eq(USER_ID), any(WorkExperienceRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/cv/me/work-experience").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).create(USER_ID, new WorkExperienceRequest("BBRZ",
                "Applikationsentwickler",
                START_DATE,
                null,
                "Beschreibung",
                null));
    }

    @Test
    void blankCompanyIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/cv/me/work-experience").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "company":"",
                                    "position":"Applikationsentwickler",
                                    "startDate":"2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void missingStartDateIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/cv/me/work-experience").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "company":"BBRZ",
                                    "position":"Applikationsentwickler"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateReturns200AndUsesThePathId() throws Exception {
        when(service.update(eq(USER_ID), eq(5L), any(WorkExperienceRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/cv/me/work-experience/5").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("BBRZ"));

        verify(service).update(eq(USER_ID), eq(5L), any(WorkExperienceRequest.class));
    }

    @Test
    void unknownEntryBecomes404() throws Exception {
        when(service.update(eq(USER_ID), eq(9999L), any(WorkExperienceRequest.class))).thenThrow(new EntryNotFoundException("Berufserfahrung 9999 nicht gefunden"));

        mockMvc.perform(put("/api/cv/me/work-experience/9999").with(token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Eintrag nicht gefunden"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/cv/me/work-experience/5").with(token()))
                .andExpect(status().isNoContent());

        verify(service).delete(USER_ID, 5L);
    }

    @Test
    void deletingAForeignEntryIs404() throws Exception {
        doThrow(new EntryNotFoundException("Berufserfahrung 5 nicht gefunden")).when(service).delete(eq(USER_ID), eq(5L));

        mockMvc.perform(delete("/api/cv/me/work-experience/5").with(token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void endDateBeforeStartDateIsRejectedBeforeTheService() throws Exception {
        mockMvc.perform(post("/api/cv/me/work-experience").with(token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "company":"BBRZ",
                                    "position":"Applikationsentwickler",
                                    "startDate":"2026-01-01",
                                    "endDate":"2025-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private WorkExperienceResponse sampleResponse() {
        return new WorkExperienceResponse(1L,
                "BBRZ",
                "Applikationsentwickler",
                START_DATE,
                null,
                "Beschreibung",
                0);
    }

    private RequestPostProcessor token() {
        return jwt().jwt(builder -> builder
                .subject(USER_ID.toString())
                .claim("username", "andreas")
                .claim("role", "TEILNEHMER"));
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