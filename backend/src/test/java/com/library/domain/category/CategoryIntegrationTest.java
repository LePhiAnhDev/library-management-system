package com.library.domain.category;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void createThenGetReturnsCategory() throws Exception {
        long id = createCategory("Science", null);
        mockMvc.perform(get("/api/v1/categories/{id}", id).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Science"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void blankNameFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/categories").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data[0].field").value("name"));
    }

    @Test
    void listFiltersBySearchTerm() throws Exception {
        createCategory("ZzzAlpha", null);
        createCategory("ZzzBeta", null);
        mockMvc.perform(get("/api/v1/categories").param("search", "Zzz").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void rejectsParentCycle() throws Exception {
        long parent = createCategory("CycleParent", null);
        long child = createCategory("CycleChild", parent);
        // Making the parent a child of its own child would create a cycle.
        mockMvc.perform(put("/api/v1/categories/{id}", parent).with(staffJwt())
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"CycleParent\",\"parentId\":" + child + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void softDeleteMarksInactive() throws Exception {
        long id = createCategory("ToDelete", null);
        mockMvc.perform(delete("/api/v1/categories/{id}", id).with(staffJwt()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/categories/{id}", id).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void cannotDeleteCategoryWithChildren() throws Exception {
        long parent = createCategory("HasChild", null);
        createCategory("TheChild", parent);
        mockMvc.perform(delete("/api/v1/categories/{id}", parent).with(staffJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    private long createCategory(String name, Long parentId) throws Exception {
        String body = parentId == null
                ? "{\"name\":\"" + name + "\"}"
                : "{\"name\":\"" + name + "\",\"parentId\":" + parentId + "}";
        String response = mockMvc.perform(post("/api/v1/categories").with(staffJwt())
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(response).path("data").path("id").asLong();
    }
}
