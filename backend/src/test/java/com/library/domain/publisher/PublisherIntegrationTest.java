package com.library.domain.publisher;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublisherIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createReturnsPublisher() throws Exception {
        mockMvc.perform(post("/api/v1/publishers").with(staffJwt())
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Penguin Books\",\"email\":\"contact@penguin.example\",\"phone\":\"0123456789\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Penguin Books"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void invalidEmailFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/publishers").with(staffJwt())
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Bad Email Co\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data[0].field").value("email"));
    }

    @Test
    void blankNameFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/publishers").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
