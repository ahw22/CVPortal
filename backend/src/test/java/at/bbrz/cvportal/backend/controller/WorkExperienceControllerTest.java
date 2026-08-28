package at.bbrz.cvportal.backend.controller;

import at.bbrz.cvportal.backend.dtos.WorkExperienceResponse;
import at.bbrz.cvportal.backend.services.WorkExperienceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkExperienceController.class)
class WorkExperienceControllerTest {

    private static final UUID USER_ID = UUID.fromString("01a0385f-b91c-7434-937d-930f09ab3001");
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkExperienceService service;

    @Test
    void findAllReturns200AndTheEntries() throws Exception{
        when(service.findAll(USER_ID)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/cv/me/work-experience").with(token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].company").value("BBRZ"))
                .andExpect(jsonPath("$[0].endDate").isEmpty());
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