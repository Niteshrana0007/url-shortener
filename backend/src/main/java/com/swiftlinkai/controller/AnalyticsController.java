package com.swiftlinkai.controller;

import com.swiftlinkai.repository.UrlAnalyticsRepository;
import com.swiftlinkai.repository.ShortUrlRepository;
import com.swiftlinkai.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Click telemetry and URL performance data")
public class AnalyticsController {

    private final UrlAnalyticsRepository analyticsRepository;
    private final ShortUrlRepository shortUrlRepository;

    @GetMapping("/{alias}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','USER','READ_ONLY')")
    @Operation(summary = "Get click summary for a URL alias")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable String alias) {
        String tenantId = TenantContext.getTenantId();

        return shortUrlRepository.findByAliasAndTenantId(alias, tenantId)
                .map(url -> {
                    long total = analyticsRepository.countByShortUrlIdAndTenantId(url.getId(), tenantId);

                    List<Object[]> byCountry = analyticsRepository.countHitsByCountry(
                            url.getId(), tenantId, LocalDateTime.now().minusDays(30));

                    List<Map<String, Object>> countryData = byCountry.stream()
                            .limit(10)
                            .map(row -> Map.of(
                                    "country", row[0] != null ? row[0] : "Unknown",
                                    "hits", row[1]))
                            .collect(Collectors.toList());

                    List<Object[]> daily = analyticsRepository.countDailyHits(
                            url.getId(), tenantId, LocalDateTime.now().minusDays(30));

                    List<Map<String, Object>> dailyData = daily.stream()
                            .map(row -> Map.of("date", row[0].toString(), "hits", row[1]))
                            .collect(Collectors.toList());

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("alias", alias);
                    response.put("totalClicks", total);
                    response.put("topCountries", countryData);
                    response.put("dailyHits", dailyData);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
