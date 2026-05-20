package com.swiftlinkai.analytics;

import com.swiftlinkai.entity.ShortUrl;
import com.swiftlinkai.entity.UrlAnalytics;
import com.swiftlinkai.repository.ShortUrlRepository;
import com.swiftlinkai.repository.UrlAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UrlAnalyticsRepository analyticsRepository;
    private final ShortUrlRepository shortUrlRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordHit(Long shortUrlId, String tenantId, String ipAddress,
                          String userAgent, String referer) {
        try {
            ShortUrl ref = shortUrlRepository.getReferenceById(shortUrlId);

            UrlAnalytics event = UrlAnalytics.builder()
                    .tenantId(tenantId)
                    .shortUrl(ref)
                    .ipAddress(anonymizeIp(ipAddress))
                    .userAgent(userAgent)
                    .referer(referer)
                    .deviceType(detectDevice(userAgent))
                    .build();

            analyticsRepository.save(event);
            shortUrlRepository.incrementClickCount(shortUrlId);
        } catch (Exception e) {
            log.error("Failed to record analytics for shortUrlId={}: {}", shortUrlId, e.getMessage());
        }
    }

    private String anonymizeIp(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".0" : ip;
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet";
        return "Desktop";
    }
}
