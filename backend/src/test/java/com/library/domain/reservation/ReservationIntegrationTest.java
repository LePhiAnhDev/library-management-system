package com.library.domain.reservation;

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

class ReservationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void cannotReserveWhenCopiesAvailable() throws Exception {
        long bookId = createBook();
        addCopy(bookId);
        long memberId = createMember();
        mockMvc.perform(post("/api/v1/reservations").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookId\":" + bookId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void fullReservationFlowReadyThenPickup() throws Exception {
        long bookId = createBook();
        long copyId = addCopy(bookId);
        long borrower = createMember();
        long reserver = createMember();

        long loanId = checkout(borrower, copyId);
        long reservationId = reserve(reserver, bookId);
        assertReservationStatus(reservationId, "PENDING");

        // Returning the only copy should ready the waiting reservation and hold the copy.
        mockMvc.perform(post("/api/v1/loans/{id}/return", loanId).with(staffJwt())
                .contentType(APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        assertReservationStatus(reservationId, "READY");
        assertCopyStatus(copyId, "RESERVED");

        // The reserving member picks up the held copy -> reservation fulfilled, copy borrowed.
        checkout(reserver, copyId);
        assertReservationStatus(reservationId, "FULFILLED");
        assertCopyStatus(copyId, "BORROWED");
    }

    @Test
    void renewBlockedWhenReservationPending() throws Exception {
        long bookId = createBook();
        long copyId = addCopy(bookId);
        long borrower = createMember();
        long reserver = createMember();
        long loanId = checkout(borrower, copyId);
        reserve(reserver, bookId);
        mockMvc.perform(post("/api/v1/loans/{id}/renew", loanId).with(staffJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void duplicateReservationBlocked() throws Exception {
        long bookId = createBook();
        long copyId = addCopy(bookId);
        long borrower = createMember();
        long reserver = createMember();
        checkout(borrower, copyId);
        reserve(reserver, bookId);
        mockMvc.perform(post("/api/v1/reservations").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + reserver + ",\"bookId\":" + bookId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    private void assertReservationStatus(long id, String expected) throws Exception {
        mockMvc.perform(get("/api/v1/reservations/{id}", id).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(expected));
    }

    private void assertCopyStatus(long copyId, String expected) throws Exception {
        mockMvc.perform(get("/api/v1/book-copies/{id}", copyId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(expected));
    }

    private long checkout(long memberId, long copyId) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long reserve(long memberId, long bookId) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/reservations").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookId\":" + bookId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long createBook() throws Exception {
        long categoryId = dataId(mockMvc.perform(post("/api/v1/categories").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ResCat" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String isbn = "RS" + (System.nanoTime() % 1_000_000_000L);
        return dataId(mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"Res Book\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long addCopy(long bookId) throws Exception {
        return dataId(mockMvc.perform(post("/api/v1/books/{id}/copies", bookId).with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"barcode\":\"RC" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long createMember() throws Exception {
        String email = "res" + unique() + "@library.local";
        return dataId(mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Res Reader\",\"email\":\"" + email + "\",\"membershipType\":\"REGULAR\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long dataId(String response) {
        return jsonMapper.readTree(response).path("data").path("id").asLong();
    }

    private static String unique() {
        return Long.toString(System.nanoTime(), 36);
    }
}
