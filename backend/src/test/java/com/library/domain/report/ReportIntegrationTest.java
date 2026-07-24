package com.library.domain.report;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void dashboardAndProfileReflectABorrow() throws Exception {
        long bookId = createBook();
        long copyId = addCopy(bookId);
        long memberId = createMember();
        checkout(memberId, copyId);

        mockMvc.perform(get("/api/v1/reports/dashboard").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBooks").isNumber())
                .andExpect(jsonPath("$.data.borrowedCount").isNumber());

        mockMvc.perform(get("/api/v1/members/{id}/profile", memberId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.member.id").value((int) memberId))
                .andExpect(jsonPath("$.data.currentLoans.length()").value(1));
    }

    @Test
    void inventoryListsCopyStatuses() throws Exception {
        long bookId = createBook();
        addCopy(bookId);
        mockMvc.perform(get("/api/v1/reports/inventory").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void loansCsvExportReturnsCsv() throws Exception {
        String body = mockMvc.perform(get("/api/v1/reports/export/loans").with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("loans.csv")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("Mã phiếu");
    }

    private long checkout(long memberId, long copyId) throws Exception {
        return dataId(mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long createBook() throws Exception {
        long categoryId = dataId(mockMvc.perform(post("/api/v1/categories").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"name\":\"RepCat" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String isbn = "RP" + (System.nanoTime() % 1_000_000_000L);
        return dataId(mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"Report Book\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long addCopy(long bookId) throws Exception {
        return dataId(mockMvc.perform(post("/api/v1/books/{id}/copies", bookId).with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"barcode\":\"RPC" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long createMember() throws Exception {
        String email = "rep" + unique() + "@library.local";
        return dataId(mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Report Reader\",\"email\":\"" + email + "\",\"membershipType\":\"REGULAR\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long dataId(String response) {
        return jsonMapper.readTree(response).path("data").path("id").asLong();
    }

    private static String unique() {
        return Long.toString(System.nanoTime(), 36);
    }
}
