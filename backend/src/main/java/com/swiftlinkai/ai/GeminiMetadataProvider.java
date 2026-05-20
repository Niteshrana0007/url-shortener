package com.swiftlinkai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Gemini Free Tier provider using Google Generative Language REST API.
 *
 * Free limits: 15 RPM, 1 million TPM, 1500 RPD (as of 2025)
 * No credit card required — just a Google account.
 * Get key: https://aistudio.google.com/apikey
 *
 * Model used: gemini-1.5-flash (fastest, most generous free quota)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiMetadataProvider implements AiMetadataProvider {

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String model;

    private final ObjectMapper objectMapper;

    private static final String BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private static final String PROMPT_TEMPLATE = """
        Analyze this URL and webpage content. Respond ONLY with valid JSON — no markdown, no explanation.

        URL: %s
        Content: %s

        Return exactly this JSON structure:
        {
          "alias": "short-seo-slug",
          "tags": ["Tag1", "Tag2", "Tag3"],
          "category": "Category",
          "seoTitle": "Page title under 60 chars",
          "seoDescription": "Description under 120 chars"
        }

        Rules:
        - alias: 2-3 lowercase words joined by hyphens, meaningful and SEO-friendly
        - tags: 2-4 relevant topic tags, Title Case
        - category: one of: Technology, Business, Health, Education, News, Entertainment, Science, Finance, Other
        - No null values, no extra fields
        """;

    @Override
    public AiMetadataResult generate(String pageContent, String url) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("GEMINI_KEY_HERE")) {
            log.warn("Gemini API key not set — returning fallback");
            return AiMetadataResult.fallback("link-" + System.currentTimeMillis());
        }

        try {
            String truncated = pageContent.length() > 2000
                    ? pageContent.substring(0, 2000) : pageContent;

            String prompt = PROMPT_TEMPLATE.formatted(url, truncated);
            String requestBody = buildRequestBody(prompt);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL.formatted(model, apiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error {}: {}", response.statusCode(), response.body());
                return AiMetadataResult.fallback("link");
            }

            return parseGeminiResponse(response.body(), url);

        } catch (Exception e) {
            log.error("Gemini call failed for {}: {}", url, e.getMessage());
            return AiMetadataResult.fallback("link");
        }
    }

    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", prompt);
        parts.add(part);
        content.put("role", "user");
        content.set("parts", parts);
        contents.add(content);
        root.set("contents", contents);

        // Generation config — keep response tight
        ObjectNode genConfig = objectMapper.createObjectNode();
        genConfig.put("temperature", 0.1);
        genConfig.put("maxOutputTokens", 2048);
        genConfig.put("responseMimeType", "application/json"); // Gemini JSON mode
        root.set("generationConfig", genConfig);

        return objectMapper.writeValueAsString(root);
    }

    private AiMetadataResult parseGeminiResponse(String responseBody, String url) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Gemini response: candidates[0].content.parts[0].text
            String text = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("");

            if (text.isBlank()) {
                log.warn("Empty Gemini response for {}", url);
                return AiMetadataResult.fallback("link");
            }

            // Strip markdown fences if present despite JSON mode
            text = text.replaceAll("(?s)```json\\s*", "")
                       .replaceAll("(?s)```\\s*", "")
                       .trim();

            JsonNode parsed = objectMapper.readTree(text);

            String alias = parsed.path("alias").asText("link");
            List<String> tags = new ArrayList<>();
            parsed.path("tags").forEach(t -> tags.add(t.asText()));
            String category    = parsed.path("category").asText("Other");
            String seoTitle    = parsed.path("seoTitle").asText(null);
            String seoDesc     = parsed.path("seoDescription").asText(null);

            log.info("Gemini generated: alias={}, category={}, tags={}", alias, category, tags);
            return new AiMetadataResult(alias, tags, category, seoTitle, seoDesc, true);

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return AiMetadataResult.fallback("link");
        }
    }

    @Override
    public String providerName() {
        return "gemini";
    }
}
