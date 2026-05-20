package com.swiftlinkai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request payload to shorten a URL")
public class ShortenUrlRequest {

    @NotBlank(message = "Long URL is required")
    @URL(message = "Must be a valid URL")
    @Size(max = 2048, message = "URL too long")
    @Schema(description = "The original long URL to be shortened",
            example = "https://example.com/very/long/article/path")
    private String longUrl;

    @Pattern(regexp = "^[a-zA-Z0-9-_]{3,50}$",
             message = "Custom alias must be 3-50 alphanumeric characters")
    @Schema(description = "Optional custom alias (auto-generated if omitted)",
            example = "my-article")
    private String customAlias;

    @Schema(description = "Expiry datetime (null for no expiry)")
    private LocalDateTime expiresAt;
}
