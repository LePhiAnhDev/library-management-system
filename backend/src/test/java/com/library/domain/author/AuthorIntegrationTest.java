package com.library.domain.author;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createReturnsAuthor() throws Exception {
        mockMvc.perform(post("/api/v1/authors").with(staffJwt())
                        .contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"George Orwell\",\"biography\":\"English novelist\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fullName").value("George Orwell"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void blankNameFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/authors").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"fullName\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void listFiltersBySearch() throws Exception {
        mockMvc.perform(post("/api/v1/authors").with(staffJwt())
                .contentType(APPLICATION_JSON).content("{\"fullName\":\"ZzqWriterOne\"}")).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/authors").with(staffJwt())
                .contentType(APPLICATION_JSON).content("{\"fullName\":\"ZzqWriterTwo\"}")).andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/authors").param("search", "Zzq").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }
}
