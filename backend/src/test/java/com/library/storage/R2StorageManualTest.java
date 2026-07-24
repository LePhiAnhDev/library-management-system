package com.library.storage;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private StorageService storageService;

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

    @Test
    void uploadsCoverWhenMultipartStreamIsNotResettable() {
        // Regression guard: a real Tomcat multipart stream is single-pass (no mark/reset), so
        // streaming it to the payload-signing SDK fails at runtime. MockMultipartFile (used in the
        // endpoint test above) is backed by a resettable ByteArrayInputStream and hides this. The
        // service must read bytes; if it reverts to streaming the InputStream, this upload throws.
        String url = storageService.uploadCover(987654L, new NonResettableMultipartFile(ONE_PIXEL_PNG));
        assertThat(url).startsWith(r2Properties.publicBaseUrl());

        String key = url.substring(r2Properties.publicBaseUrl().length() + 1);
        r2Client.deleteObject(DeleteObjectRequest.builder().bucket(r2Properties.bucketName()).key(key).build());
    }

    /** MultipartFile whose InputStream mimics Tomcat's: readable once, no mark/reset. */
    private static final class NonResettableMultipartFile implements MultipartFile {
        private final byte[] content;

        NonResettableMultipartFile(byte[] content) {
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return "cover.png";
        }

        @Override
        public String getContentType() {
            return "image/png";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new FilterInputStream(new ByteArrayInputStream(content)) {
                @Override
                public boolean markSupported() {
                    return false;
                }

                @Override
                public void mark(int readlimit) {
                    // no-op: unsupported
                }

                @Override
                public void reset() throws IOException {
                    throw new IOException("mark/reset not supported");
                }
            };
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException();
        }
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
