package at.bbrz.cvportal.backend.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        GlobalExceptionManager.class,
        GlobalExceptionManagerTest.BoomController.class})
class GlobalExceptionManagerTest {

    @Autowired
    MockMvc mockMvc;

    @RestController
    static class BoomController {
        @GetMapping("/test/boom")
        String boom() {
            throw new IllegalStateException("kaputt");
        }
    }

    @Test
    void unexpectedExceptionBecomes500WithNeutralMessage() throws Exception {
        String body = mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Interner Fehler"))
                .andReturn().getResponse().getContentAsString();

        assertFalse(body.contains("kaputt"));
    }

}