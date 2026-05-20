package com.swiftlinkai.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAIServiceTest {

    @Mock
    private AiMetadataProvider aiMetadataProvider;

    private GenAIService genAIService;

    @BeforeEach
    void setUp() {
        genAIService = new GenAIService(aiMetadataProvider);
    }

    @Test
    @DisplayName("generateMetadata() should return AI result on success")
    void generateMetadata_success() throws ExecutionException, InterruptedException {
        AiMetadataResult expected = new AiMetadataResult(
                "test-alias", List.of("Tech"), "Technology", "Title", "Desc", true);

        when(aiMetadataProvider.generate(anyString(), anyString())).thenReturn(expected);

        AiMetadataResult result = genAIService
                .generateMetadata("https://example.com")
                .get();

        assertThat(result.alias()).isEqualTo("test-alias");
        assertThat(result.aiGenerated()).isTrue();
        assertThat(result.tags()).containsExactly("Tech");
    }

    @Test
    @DisplayName("fallbackMetadata() should return non-null alias")
    void fallbackMetadata_returnsAlias() throws ExecutionException, InterruptedException {
        AiMetadataResult result = genAIService
                .fallbackMetadata("https://example.com", new RuntimeException("timeout"))
                .get();

        assertThat(result).isNotNull();
        assertThat(result.alias()).isNotBlank();
        assertThat(result.aiGenerated()).isFalse();
    }
}
