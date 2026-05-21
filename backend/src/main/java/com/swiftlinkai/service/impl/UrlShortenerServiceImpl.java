package com.swiftlinkai.service.impl;

import com.swiftlinkai.ai.AiMetadataResult;
import com.swiftlinkai.ai.GenAIService;
import com.swiftlinkai.cache.RedirectCacheService;
import com.swiftlinkai.dto.request.ShortenUrlRequest;
import com.swiftlinkai.dto.response.ShortenUrlResponse;
import com.swiftlinkai.entity.ShortUrl;
import com.swiftlinkai.entity.User;
import com.swiftlinkai.exception.AliasConflictException;
import com.swiftlinkai.exception.ResourceNotFoundException;
import com.swiftlinkai.mapper.ShortUrlMapper;
import com.swiftlinkai.repository.ShortUrlRepository;
import com.swiftlinkai.repository.UserRepository;
import com.swiftlinkai.service.UrlShortenerService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final GenAIService genAIService;
    private final RedirectCacheService redirectCacheService;
    private final ShortUrlMapper shortUrlMapper;
    private final MeterRegistry meterRegistry;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public ShortenUrlResponse shorten(ShortenUrlRequest request, String userId, String tenantId) {
        Timer.Sample sample = Timer.start(meterRegistry);

        // Normalize URL
        String normalizedUrl = normalizeUrl(request.getLongUrl());

        // Determine alias
        String alias = resolveAlias(request, tenantId);

        // Fetch user
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Trigger AI pipeline (non-blocking, with timeout)
        AiMetadataResult aiResult = runAiPipeline(normalizedUrl, alias);

        // Persist
        ShortUrl shortUrl = ShortUrl.builder()
                .tenantId(tenantId)
                .alias(aiResult.alias())
                .originalUrl(normalizedUrl)
                .generatedTags(aiResult.tags())
                .category(aiResult.category())
                .seoTitle(aiResult.seoTitle())
                .seoDescription(aiResult.seoDescription())
                .createdBy(user)
                .expiresAt(request.getExpiresAt())
                .build();

        shortUrl = shortUrlRepository.save(shortUrl);

        // Warm cache
        redirectCacheService.put(shortUrl.getAlias(), shortUrl.getOriginalUrl());

        sample.stop(meterRegistry.timer("url.shorten.duration", "tenant", tenantId));
        Counter.builder("url.shortened.total").tag("tenant", tenantId).register(meterRegistry).increment();

        log.info("URL shortened: alias={}, tenant={}, aiGenerated={}", alias, tenantId, aiResult.aiGenerated());
        return buildResponse(shortUrl, aiResult.aiGenerated());
    }

    @Override
    public String resolveAlias(String alias) {
        // 1. Redis lookup
        String cached = redirectCacheService.get(alias);
        if (cached != null) {
            log.debug("Cache hit for alias: {}", alias);
            return cached;
        }

        // 2. DB fallback
        ShortUrl shortUrl = shortUrlRepository.findByAlias(alias)
                .orElseThrow(() -> new ResourceNotFoundException("Alias not found: " + alias));

        if (!shortUrl.getIsActive()) {
            throw new ResourceNotFoundException("Alias is deactivated: " + alias);
        }
        if (shortUrl.isExpired()) {
            throw new ResourceNotFoundException("Alias has expired: " + alias);
        }

        // Re-warm cache
        redirectCacheService.put(alias, shortUrl.getOriginalUrl());
        return shortUrl.getOriginalUrl();
    }

    @Override
    public Page<ShortenUrlResponse> listUrls(String tenantId, Pageable pageable) {
        return shortUrlRepository.findActiveByTenantId(tenantId, LocalDateTime.now(), pageable)
                .map(url -> buildResponse(url, false));
    }

    @Override
    @Transactional
    public void deactivateUrl(String alias, String tenantId) {
        ShortUrl url = shortUrlRepository.findByAliasAndTenantId(alias, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found: " + alias));
        url.setIsActive(false);
        shortUrlRepository.save(url);
        redirectCacheService.evict(alias);
    }

    @Override
    public ShortenUrlResponse getUrlDetails(String alias, String tenantId) {
        ShortUrl url = shortUrlRepository.findByAliasAndTenantId(alias, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found: " + alias));
        return buildResponse(url, false);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String resolveAlias(ShortenUrlRequest request, String tenantId) {
        if (StringUtils.hasText(request.getCustomAlias())) {
            if (shortUrlRepository.existsByAlias(request.getCustomAlias())) {
                throw new AliasConflictException("Alias already taken: " + request.getCustomAlias());
            }
            return request.getCustomAlias();
        }
        return null; // AI will generate
    }

    private AiMetadataResult runAiPipeline(String url, String preferredAlias) {
        try {
            AiMetadataResult result = genAIService.generateMetadata(url)
                    .get(10, TimeUnit.SECONDS);

            // Override alias if custom was provided
            if (preferredAlias != null) {
                return new AiMetadataResult(preferredAlias, result.tags(), result.category(),
                        result.seoTitle(), result.seoDescription(), result.aiGenerated());
            }

            // Ensure alias uniqueness
            String uniqueAlias = ensureUniqueAlias(result.alias());
            return new AiMetadataResult(uniqueAlias, result.tags(), result.category(),
                    result.seoTitle(), result.seoDescription(), result.aiGenerated());

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("AI pipeline failed for {}: {}", url, e.getMessage());
            String fallback = preferredAlias != null ? preferredAlias : "lnk-" + System.currentTimeMillis();
            return AiMetadataResult.fallback(ensureUniqueAlias(fallback));
        }
    }

    private String ensureUniqueAlias(String alias) {
        String candidate = alias;
        int attempts = 0;
        while (shortUrlRepository.existsByAlias(candidate) && attempts < 5) {
            candidate = alias + "-" + (attempts + 2);
            attempts++;
        }
        return candidate;
    }

    private String normalizeUrl(String url) {
        return url.trim().replaceAll("\\s+", "");
    }

    private ShortenUrlResponse buildResponse(ShortUrl shortUrl, boolean aiGenerated) {
        return ShortenUrlResponse.builder()
                .shortUrl(baseUrl + "/api/v1/" + shortUrl.getAlias())
                .alias(shortUrl.getAlias())
                .originalUrl(shortUrl.getOriginalUrl())
                .tags(shortUrl.getGeneratedTags())
                .category(shortUrl.getCategory())
                .seoTitle(shortUrl.getSeoTitle())
                .qrCodeUrl(shortUrl.getQrCodeUrl())
                .expiresAt(shortUrl.getExpiresAt())
                .createdAt(shortUrl.getCreatedAt())
                .aiGenerated(aiGenerated)
                .build();
    }
}
