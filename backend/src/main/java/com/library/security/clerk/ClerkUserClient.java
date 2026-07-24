package com.library.security.clerk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * Fetches a Clerk user by id from the Backend API. The session JWT only carries the subject,
 * so email, name and avatar are read from Clerk once at provisioning time and cached locally.
 * Parsing is defensive (by JSON path) so a Clerk shape change or outage never breaks authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClerkUserClient {

    private final RestClient clerkRestClient;
    private final JsonMapper jsonMapper;

    public Optional<ClerkUserInfo> fetchUser(String clerkUserId) {
        try {
            String body = clerkRestClient.get()
                    .uri("/users/{id}", clerkUserId)
                    .retrieve()
                    .body(String.class);
            if (body == null || body.isBlank()) {
                return Optional.empty();
            }
            JsonNode root = jsonMapper.readTree(body);
            return Optional.of(new ClerkUserInfo(extractEmail(root), extractFullName(root), text(root, "image_url")));
        } catch (Exception ex) {
            log.warn("Không lấy được thông tin người dùng Clerk {}: {}", clerkUserId, ex.getMessage());
            return Optional.empty();
        }
    }

    private static String extractEmail(JsonNode root) {
        String primaryId = text(root, "primary_email_address_id");
        JsonNode emails = root.path("email_addresses");
        if (!emails.isArray() || emails.isEmpty()) {
            return null;
        }
        if (primaryId != null) {
            for (JsonNode entry : emails) {
                if (primaryId.equals(text(entry, "id"))) {
                    return text(entry, "email_address");
                }
            }
        }
        return text(emails.get(0), "email_address");
    }

    private static String extractFullName(JsonNode root) {
        String first = text(root, "first_name");
        String last = text(root, "last_name");
        String full = ((first == null ? "" : first.trim()) + " " + (last == null ? "" : last.trim())).trim();
        return full.isEmpty() ? null : full;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
