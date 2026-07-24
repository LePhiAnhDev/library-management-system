package com.library.storage;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real Cloudflare R2 upload path (endpoint -> StorageService -> R2 -> DB).
 * Tagged "r2" so it is excluded from the normal suite; run with -DexcludedGroups= to include it.
 */
@Tag("r2")
class R2StorageManualTest extends AbstractIntegrationTest {

    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M8AAAMBAQAY3Y2wAAAAAElFTkSuQmCC");

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private S3Client r2Client;

    @Autowired
    private R2Properties r2Properties;

    @Test
    void uploadsCoverThroughEndpointToRealR2() throws Exception {
        long categoryId = createCategory();
        long bookId = createBook(categoryId);

        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", ONE_PIXEL_PNG);
        String response = mockMvc.perform(multipart("/api/v1/books/{id}/cover", bookId).file(file).with(staffJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverImageUrl").exists())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String url = jsonMapper.readTree(response).path("data").path("coverImageUrl").asText();
        assertThat(url).startsWith(r2Properties.publicBaseUrl());

        // Cleanup: remove the uploaded object from R2.
        String key = url.substring(r2Properties.publicBaseUrl().length() + 1);
        r2Client.deleteObject(DeleteObjectRequest.builder().bucket(r2Properties.bucketName()).key(key).build());
    }

    private long createCategory() throws Exception {
        String resp = mockMvc.perform(post("/api/v1/categories").with(staffJwt())
                        .contentType(APPLICATION_JSON).content("{\"name\":\"R2Cat\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("id").asLong();
    }

    private long createBook(long categoryId) throws Exception {
        String isbn = "R2" + (System.nanoTime() % 1_000_000_000L);
        String resp = mockMvc.perform(post("/api/v1/books").with(staffJwt()).contentType(APPLICATION_JSON)
                        .content("{\"isbn\":\"" + isbn + "\",\"title\":\"Cover Book\",\"categoryId\":" + categoryId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return jsonMapper.readTree(resp).path("data").path("id").asLong();
    }
}
