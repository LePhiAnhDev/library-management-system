package com.library.domain.book;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void createBookWithCategoryAndAuthor() throws Exception {
        long categoryId = createCategory("Fiction-" + unique());
        long authorId = createAuthor("Author-" + unique());
        String isbn = "978" + System.nanoTime() % 1_000_000_0000L;
        String body = "{\"isbn\":\"" + isbn + "\",\"title\":\"Nineteen Eighty-Four\",\"categoryId\":" + categoryId
                + ",\"authorIds\":[" + authorId + "],\"publicationYear\":1949,\"pageCount\":328}";
        String resp = mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Nineteen Eighty-Four"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalCopies").value(0))
                .andExpect(jsonPath("$.data.availableCopies").value(0))
                .andExpect(jsonPath("$.data.authors[0].id").value((int) authorId))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long bookId = dataId(resp);
        mockMvc.perform(get("/api/v1/books/{id}", bookId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value((int) categoryId));
    }

    @Test
    void duplicateIsbnConflicts() throws Exception {
        long categoryId = createCategory("Cat-" + unique());
        String isbn = "111" + System.nanoTime() % 1_000_000_0000L;
        createBook(isbn, "First", categoryId);
        mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"Second\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void missingCategoryFailsValidation() throws Exception {
        mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"222" + (System.nanoTime() % 1_000_000L) + "\",\"title\":\"No Category\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void copyLifecycleKeepsCountsConsistent() throws Exception {
        long categoryId = createCategory("Cat-" + unique());
        long bookId = createBook("333" + System.nanoTime() % 1_000_000_0000L, "Counted", categoryId);

        long copy1 = addCopy(bookId, "BC-" + unique());
        addCopy(bookId, "BC-" + unique());
        assertCounts(bookId, 2, 2);

        // Mark one copy under maintenance -> available drops, total unchanged.
        mockMvc.perform(post("/api/v1/book-copies/{id}/status", copy1).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));
        assertCounts(bookId, 2, 1);

        // Delete the maintenance copy -> total and available reflect it.
        mockMvc.perform(delete("/api/v1/book-copies/{id}", copy1).with(staffJwt()))
                .andExpect(status().isNoContent());
        assertCounts(bookId, 1, 1);
    }

    @Test
    void availableFilterReturnsOnlyBooksWithCopies() throws Exception {
        long categoryId = createCategory("Avail-" + unique());
        long withCopies = createBook("444" + System.nanoTime() % 1_000_000_0000L, "HasCopies", categoryId);
        addCopy(withCopies, "AV-" + unique());
        createBook("445" + System.nanoTime() % 1_000_000_0000L, "NoCopies", categoryId);

        String resp = mockMvc.perform(get("/api/v1/books").param("categoryId", String.valueOf(categoryId))
                        .param("available", "true").with(staffJwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode content = jsonMapper.readTree(resp).path("data").path("content");
        // Only the book with an available copy should appear for this category.
        for (JsonNode book : content) {
            org.junit.jupiter.api.Assertions.assertTrue(book.path("availableCopies").asInt() > 0);
        }
    }

    @Test
    void barcodeDuplicateConflicts() throws Exception {
        long categoryId = createCategory("Cat-" + unique());
        long bookId = createBook("555" + System.nanoTime() % 1_000_000_0000L, "Dup Barcode", categoryId);
        String barcode = "DUP-" + unique();
        addCopy(bookId, barcode);
        mockMvc.perform(post("/api/v1/books/{id}/copies", bookId).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"barcode\":\"" + barcode + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void categoryDeleteBlockedWhenReferencedByBook() throws Exception {
        long categoryId = createCategory("Referenced-" + unique());
        createBook("666" + System.nanoTime() % 1_000_000_0000L, "Ref", categoryId);
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId).with(staffJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    private void assertCounts(long bookId, int total, int available) throws Exception {
        mockMvc.perform(get("/api/v1/books/{id}", bookId).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCopies").value(total))
                .andExpect(jsonPath("$.data.availableCopies").value(available));
    }

    private long createCategory(String name) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/categories").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long createAuthor(String name) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/authors").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"fullName\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long createBook(String isbn, String title, long categoryId) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"" + title + "\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long addCopy(long bookId, String barcode) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/books/{id}/copies", bookId).with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"barcode\":\"" + barcode + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return dataId(resp);
    }

    private long dataId(String response) {
        return jsonMapper.readTree(response).path("data").path("id").asLong();
    }

    private static String unique() {
        return Long.toString(System.nanoTime(), 36);
    }
}
