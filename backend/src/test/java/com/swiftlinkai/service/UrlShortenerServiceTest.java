package com.swiftlinkai.service;

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
import com.swiftlinkai.service.impl.UrlShortenerServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock private ShortUrlRepository shortUrlRepository;
    @Mock private UserRepository userRepository;
    @Mock private GenAIService genAIService;
    @Mock private RedirectCacheService redirectCacheService;
    @Mock private ShortUrlMapper shortUrlMapper;

    private MeterRegistry meterRegistry;
    private UrlShortenerServiceImpl service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new UrlShortenerServiceImpl(
                shortUrlRepository, userRepository, genAIService,
                redirectCacheService, shortUrlMapper, meterRegistry);
        ReflectionTestUtils.setField(service, "baseUrl", "https://swiftlink.ai");
    }

    @Test
    @DisplayName("shorten() should persist URL and return response with AI metadata")
    void shorten_success() {
        // Given
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setLongUrl("https://example.com/article");

        User user = User.builder().id(1L).tenantId("tenant-1").email("user@test.com")
                .role(User.Role.USER).build();

        AiMetadataResult aiResult = new AiMetadataResult(
                "example-article", List.of("Tech"), "Technology", "Title", "Desc", true);

        ShortUrl saved = ShortUrl.builder()
                .id(1L).alias("example-article").tenantId("tenant-1")
                .originalUrl("https://example.com/article")
                .generatedTags(List.of("Tech")).category("Technology").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shortUrlRepository.existsByAlias(any())).thenReturn(false);
        when(genAIService.generateMetadata(any())).thenReturn(CompletableFuture.completedFuture(aiResult));
        when(shortUrlRepository.save(any())).thenReturn(saved);

        // When
        ShortenUrlResponse result = service.shorten(request, "1", "tenant-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAlias()).isEqualTo("example-article");
        assertThat(result.isAiGenerated()).isTrue();
        verify(redirectCacheService).put(eq("example-article"), any());
    }

    @Test
    @DisplayName("shorten() with duplicate custom alias should throw AliasConflictException")
    void shorten_duplicateAlias_throwsConflict() {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setLongUrl("https://example.com");
        request.setCustomAlias("taken");

        when(shortUrlRepository.existsByAlias("taken")).thenReturn(true);

        assertThatThrownBy(() -> service.shorten(request, "1", "tenant-1"))
                .isInstanceOf(AliasConflictException.class)
                .hasMessageContaining("taken");
    }

    @Test
    @DisplayName("resolveAlias() should return cached URL if available")
    void resolveAlias_cacheHit() {
        when(redirectCacheService.get("my-alias")).thenReturn("https://original.com");

        String result = service.resolveAlias("my-alias");

        assertThat(result).isEqualTo("https://original.com");
        verify(shortUrlRepository, never()).findByAlias(any());
    }

    @Test
    @DisplayName("resolveAlias() should fallback to DB on cache miss")
    void resolveAlias_cacheMiss_dbHit() {
        ShortUrl url = ShortUrl.builder()
                .alias("db-alias").originalUrl("https://db.com")
                .isActive(true).build();

        when(redirectCacheService.get("db-alias")).thenReturn(null);
        when(shortUrlRepository.findByAlias("db-alias")).thenReturn(Optional.of(url));

        String result = service.resolveAlias("db-alias");

        assertThat(result).isEqualTo("https://db.com");
        verify(redirectCacheService).put("db-alias", "https://db.com");
    }

    @Test
    @DisplayName("resolveAlias() should throw when alias not found")
    void resolveAlias_notFound() {
        when(redirectCacheService.get("missing")).thenReturn(null);
        when(shortUrlRepository.findByAlias("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveAlias("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
