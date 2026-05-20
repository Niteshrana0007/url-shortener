// package com.swiftlinkai.ai;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.stereotype.Component;

// import java.util.ArrayList;
// import java.util.List;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class OpenAiMetadataProvider implements AiMetadataProvider {

//     private final ChatClient chatClient;
//     private final ObjectMapper objectMapper;

//     private static final String PROMPT_TEMPLATE = """
//             Analyze the following webpage content and URL. Respond ONLY with valid JSON.
            
//             URL: %s
//             Content (first 2000 chars): %s
            
//             Return JSON with this exact structure:
//             {
//               "alias": "short-seo-slug-3-to-5-words",
//               "tags": ["tag1", "tag2", "tag3"],
//               "category": "Single Category",
//               "seoTitle": "Concise SEO title under 60 chars",
//               "seoDescription": "Meta description under 160 chars"
//             }
            
//             Rules:
//             - alias: lowercase, hyphens only, 3-5 meaningful words, no offensive terms
//             - tags: 2-5 relevant tags, title case
//             - category: single broad category (Technology, Business, Health, etc.)
//             - All fields required, no nulls
//             """;

//     @Override
//     public AiMetadataResult generate(String pageContent, String url) {
//         String truncatedContent = pageContent.length() > 2000
//                 ? pageContent.substring(0, 2000)
//                 : pageContent;

//         String prompt = PROMPT_TEMPLATE.formatted(url, truncatedContent);

//         String rawResponse = chatClient.prompt()
//                 .user(prompt)
//                 .call()
//                 .content();

//         return parseResponse(rawResponse, url);
//     }

//     private AiMetadataResult parseResponse(String rawJson, String url) {
//         try {
//             // Strip markdown fences if present
//             String clean = rawJson.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
//             JsonNode node = objectMapper.readTree(clean);

//             String alias = node.path("alias").asText("link");
//             List<String> tags = new ArrayList<>();
//             node.path("tags").forEach(t -> tags.add(t.asText()));
//             String category = node.path("category").asText("General");
//             String seoTitle = node.path("seoTitle").asText(null);
//             String seoDescription = node.path("seoDescription").asText(null);

//             return new AiMetadataResult(alias, tags, category, seoTitle, seoDescription, true);
//         } catch (Exception e) {
//             log.warn("Failed to parse AI response for URL {}: {}", url, e.getMessage());
//             return AiMetadataResult.fallback("link-" + System.currentTimeMillis());
//         }
//     }

//     @Override
//     public String providerName() {
//         return "openai";
//     }
// }
