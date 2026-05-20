package com.swiftlinkai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response after successfully shortening a URL")
public class ShortenUrlResponse {

    @Schema(description = "The full short URL", example = "https://swiftlink.ai/ai-news")
    private String shortUrl;

    @Schema(description = "The short alias only", example = "ai-news")
    private String alias;

    @Schema(description = "Original long URL")
    private String originalUrl;

    @Schema(description = "AI-generated tags")
    private List<String> tags;

    @Schema(description = "AI-generated category")
    private String category;

    @Schema(description = "SEO-friendly title")
    private String seoTitle;

    @Schema(description = "QR code image URL")
    private String qrCodeUrl;

    @Schema(description = "Expiry timestamp (null = never expires)")
    private LocalDateTime expiresAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Whether AI metadata was generated")
    private boolean aiGenerated;
}
