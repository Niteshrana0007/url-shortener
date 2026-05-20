package com.swiftlinkai.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenAIService {

    private final AiMetadataProvider aiMetadataProvider;

    private static final String CB_NAME = "aiService";

    /**
     * Full AI pipeline: fetch page → extract content → call LLM.
     * Protected by circuit breaker, retry, and time limiter.
     */
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackMetadata")
    @Retry(name = CB_NAME)
    @TimeLimiter(name = CB_NAME)
    public CompletableFuture<AiMetadataResult> generateMetadata(String url) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting AI metadata generation for URL: {}", url);
            String pageContent = fetchPageContent(url);
            AiMetadataResult result = aiMetadataProvider.generate(pageContent, url);
            log.info("AI metadata generated: alias={}, category={}", result.alias(), result.category());
            return result;
        });
    }

    /**
     * Fallback: generate a base62 alias when AI is unavailable.
     */
    public CompletableFuture<AiMetadataResult> fallbackMetadata(String url, Throwable t) {
        log.warn("AI service unavailable for URL {}, using fallback. Cause: {}", url, t.getMessage());
        String fallbackAlias = generateBase62Alias();
        return CompletableFuture.completedFuture(AiMetadataResult.fallback(fallbackAlias));
    }

    private String fetchPageContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();
            // Extract meaningful text: title + meta description + first body paragraphs
            String title = doc.title();
            String metaDesc = doc.select("meta[name=description]").attr("content");
            String bodyText = doc.body() != null ? doc.body().text() : "";
            String combined = title + " " + metaDesc + " " + bodyText;
            return combined.length() > 3000 ? combined.substring(0, 3000) : combined;
        } catch (IOException e) {
            log.warn("Could not fetch page content for {}: {}", url, e.getMessage());
            return url; // Fall back to using the URL itself as context
        }
    }

    private String generateBase62Alias() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return toBase62(Long.parseUnsignedLong(uuid.substring(0, 15), 16));
    }

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private String toBase62(long value) {
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.insert(0, BASE62_CHARS.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.toString();
    }
}
