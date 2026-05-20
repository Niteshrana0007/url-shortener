package com.swiftlinkai.controller;

import com.swiftlinkai.analytics.AnalyticsService;
import com.swiftlinkai.dto.request.ShortenUrlRequest;
import com.swiftlinkai.dto.response.ShortenUrlResponse;
import com.swiftlinkai.exception.RateLimitExceededException;
import com.swiftlinkai.rate_limit.RateLimitService;
import com.swiftlinkai.repository.ShortUrlRepository;
import com.swiftlinkai.security.TenantContext;
import com.swiftlinkai.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "URL Shortener", description = "Core URL shortening and redirect endpoints")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;
    private final RateLimitService rateLimitService;
    private final ShortUrlRepository shortUrlRepository;

    @PostMapping("/shorten")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Shorten a URL with AI-generated metadata",
               responses = {
                   @ApiResponse(responseCode = "201", description = "URL shortened successfully"),
                   @ApiResponse(responseCode = "409", description = "Alias already taken"),
                   @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
               })
    public ResponseEntity<ShortenUrlResponse> shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        if (!rateLimitService.allowShorten(ip)) {
            throw new RateLimitExceededException("Shorten rate limit exceeded");
        }

        String userId = TenantContext.getUserId();
        String tenantId = TenantContext.getTenantId();

        ShortenUrlResponse response = urlShortenerService.shorten(request, userId, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{alias}")
    @Operation(summary = "Resolve alias and redirect",
               responses = {
                   @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
                   @ApiResponse(responseCode = "404", description = "Alias not found")
               })
    public ResponseEntity<Void> redirect(
            @PathVariable String alias,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        if (!rateLimitService.allowRedirect(ip)) {
            throw new RateLimitExceededException("Redirect rate limit exceeded");
        }

        String originalUrl = urlShortenerService.resolveAlias(alias);

        // Async analytics (fire-and-forget)
        shortUrlRepository.findByAlias(alias).ifPresent(url ->
            analyticsService.recordHit(
                url.getId(),
                url.getTenantId(),
                ip,
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
            )
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

    @GetMapping("/urls")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "List all URLs for the current tenant")
    public ResponseEntity<Page<ShortenUrlResponse>> listUrls(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(urlShortenerService.listUrls(tenantId, pageable));
    }

    @GetMapping("/urls/{alias}")
    @PreAuthorize("hasAnyRole('ADMIN','USER','READ_ONLY')")
    @Operation(summary = "Get URL details by alias")
    public ResponseEntity<ShortenUrlResponse> getUrlDetails(@PathVariable String alias) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(urlShortenerService.getUrlDetails(alias, tenantId));
    }

    @DeleteMapping("/urls/{alias}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Deactivate a shortened URL")
    public ResponseEntity<Void> deactivate(@PathVariable String alias) {
        String tenantId = TenantContext.getTenantId();
        urlShortenerService.deactivateUrl(alias, tenantId);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
