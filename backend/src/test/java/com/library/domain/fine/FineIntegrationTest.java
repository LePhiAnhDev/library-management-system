package com.library.domain.fine;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FineIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void settleIsIdempotent() throws Exception {
        long fineId = createManualFine(createMember(), "15000");
        mockMvc.perform(post("/api/v1/fines/{id}/settle", fineId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
        // Settling again must remain PAID and not error (retry-safe).
        mockMvc.perform(post("/api/v1/fines/{id}/settle", fineId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void waiveMarksWaived() throws Exception {
        long fineId = createManualFine(createMember(), "20000");
        mockMvc.perform(post("/api/v1/fines/{id}/waive", fineId).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"reason\":\"Độc giả khiếu nại hợp lý\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WAIVED"));
    }

    @Test
    void listFiltersByMemberAndStatus() throws Exception {
        long memberId = createMember();
        createManualFine(memberId, "5000");
        mockMvc.perform(get("/api/v1/fines").param("memberId", String.valueOf(memberId))
                        .param("status", "UNPAID").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("UNPAID"));
    }

    @Test
    void manualFineRequiresPositiveAmount() throws Exception {
        long memberId = createMember();
        mockMvc.perform(post("/api/v1/fines").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"type\":\"OVERDUE\",\"amount\":-1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    private long createManualFine(long memberId, String amount) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/fines").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"type\":\"DAMAGED\",\"amount\":" + amount + ",\"reason\":\"Test\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("id").asLong();
    }

    private long createMember() throws Exception {
        String email = "fine" + Long.toString(System.nanoTime(), 36) + "@library.local";
        String resp = mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Fine Reader\",\"email\":\"" + email + "\",\"membershipType\":\"REGULAR\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("id").asLong();
    }
}
