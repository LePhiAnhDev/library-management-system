package com.library.domain.member;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void createGeneratesCodeAndDefaults() throws Exception {
        String email = "reader" + System.nanoTime() + "@library.local";
        mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Nguyen Van A\",\"email\":\"" + email + "\",\"membershipType\":\"STUDENT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.memberCode", startsWith("MB")))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.joinDate").exists())
                .andExpect(jsonPath("$.data.expiryDate").exists())
                .andExpect(jsonPath("$.data.membershipType").value("STUDENT"));
    }

    @Test
    void duplicateEmailConflicts() throws Exception {
        String email = "dup" + System.nanoTime() + "@library.local";
        create(email, "PREMIUM");
        mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Another\",\"email\":\"" + email + "\",\"membershipType\":\"REGULAR\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void invalidEmailFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Bad\",\"email\":\"nope\",\"membershipType\":\"REGULAR\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void changeStatusSuspends() throws Exception {
        long id = create("suspend" + System.nanoTime() + "@library.local", "REGULAR");
        mockMvc.perform(post("/api/v1/members/{id}/status", id).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    void listFiltersByType() throws Exception {
        create("pf1" + System.nanoTime() + "@library.local", "PREMIUM");
        mockMvc.perform(get("/api/v1/members").param("membershipType", "PREMIUM").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].membershipType").value("PREMIUM"));
    }

    private long create(String email, String type) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Reader\",\"email\":\"" + email + "\",\"membershipType\":\"" + type + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("id").asLong();
    }
}
