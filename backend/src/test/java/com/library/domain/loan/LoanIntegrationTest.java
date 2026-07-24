package com.library.domain.loan;

import com.library.domain.fine.FineRepository;
import com.library.domain.fine.FineStatus;
import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineRepository fineRepository;

    @Test
    void checkoutRenewReturnHappyPath() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long copyId = addCopy(bookId);
        long memberId = createMember("REGULAR");

        String checkout = mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("BORROWED"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long loanId = dataId(checkout);
        assertThat(availableCopies(bookId)).isZero();

        mockMvc.perform(post("/api/v1/loans/{id}/renew", loanId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.renewCount").value(1));

        mockMvc.perform(post("/api/v1/loans/{id}/return", loanId).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"));
        assertThat(availableCopies(bookId)).isEqualTo(1);
    }

    @Test
    void suspendedMemberCannotBorrow() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long copyId = addCopy(bookId);
        long memberId = createMember("REGULAR");
        mockMvc.perform(post("/api/v1/members/{id}/status", memberId).with(staffJwt())
                .contentType(APPLICATION_JSON).content("{\"status\":\"SUSPENDED\"}")).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void borrowingBeyondPolicyLimitIsBlocked() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long memberId = createMember("REGULAR"); // REGULAR max = 3
        for (int i = 0; i < 3; i++) {
            long copyId = addCopy(bookId);
            mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                    .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}")).andExpect(status().isCreated());
        }
        long extraCopy = addCopy(bookId);
        mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + extraCopy + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void lateReturnRaisesOverdueFine() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long copyId = addCopy(bookId);
        long memberId = createMember("REGULAR");
        long loanId = dataId(mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        // Backdate the due date so the return is late.
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        loan.setDueDate(LocalDate.now(ZoneOffset.UTC).minusDays(5));
        loanRepository.save(loan);

        mockMvc.perform(post("/api/v1/loans/{id}/return", loanId).with(staffJwt())
                .contentType(APPLICATION_JSON).content("{}")).andExpect(status().isOk());

        BigDecimal unpaid = fineRepository.sumByMemberAndStatus(memberId, FineStatus.UNPAID);
        assertThat(unpaid).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void lostReturnMarksCopyLostAndFines() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long copyId = addCopy(bookId);
        long memberId = createMember("REGULAR");
        long loanId = dataId(mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/loans/{id}/return", loanId).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"condition\":\"LOST\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/book-copies/{id}", copyId).with(staffJwt()))
                .andExpect(jsonPath("$.data.status").value("LOST"));
        assertThat(fineRepository.sumByMemberAndStatus(memberId, FineStatus.UNPAID)).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void concurrentCheckoutOfSameCopyAllowsExactlyOne() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);
        long copyId = addCopy(bookId);
        long member1 = createMember("REGULAR");
        long member2 = createMember("REGULAR");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> t1 = () -> {
            start.await();
            return checkoutStatus(member1, copyId);
        };
        Callable<Integer> t2 = () -> {
            start.await();
            return checkoutStatus(member2, copyId);
        };
        Future<Integer> f1 = pool.submit(t1);
        Future<Integer> f2 = pool.submit(t2);
        start.countDown();
        int s1 = f1.get(30, TimeUnit.SECONDS);
        int s2 = f2.get(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        long created = List.of(s1, s2).stream().filter(s -> s == 201).count();
        assertThat(created).as("exactly one checkout should succeed").isEqualTo(1);
        // The copy started with availability 1; exactly one decrement must have happened.
        assertThat(availableCopies(bookId)).as("no double borrow, counter accurate").isZero();
    }

    private int checkoutStatus(long memberId, long copyId) throws Exception {
        return mockMvc.perform(post("/api/v1/loans").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"memberId\":" + memberId + ",\"bookCopyId\":" + copyId + "}"))
                .andReturn().getResponse().getStatus();
    }

    private int availableCopies(long bookId) throws Exception {
        String resp = mockMvc.perform(get("/api/v1/books/{id}", bookId).with(staffJwt()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("availableCopies").asInt();
    }

    private long createCategory() throws Exception {
        return dataId(mockMvc.perform(post("/api/v1/categories").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"name\":\"LoanCat" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long createBook(long categoryId) throws Exception {
        String isbn = "LN" + (System.nanoTime() % 1_000_000_000L);
        return dataId(mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"Loan Book\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long addCopy(long bookId) throws Exception {
        return dataId(mockMvc.perform(post("/api/v1/books/{id}/copies", bookId).with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"barcode\":\"BC" + unique() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long createMember(String type) throws Exception {
        String email = "loan" + unique() + "@library.local";
        return dataId(mockMvc.perform(post("/api/v1/members").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Loan Reader\",\"email\":\"" + email + "\",\"membershipType\":\"" + type + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long dataId(String response) {
        return jsonMapper.readTree(response).path("data").path("id").asLong();
    }

    private static String unique() {
        return Long.toString(System.nanoTime(), 36);
    }
}
