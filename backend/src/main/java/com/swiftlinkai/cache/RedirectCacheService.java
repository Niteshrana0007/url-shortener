package com.swiftlinkai.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectCacheService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.cache.redirect-ttl-hours:24}")
    private long redirectTtlHours;

    private static final String PREFIX = "redirect:";

    public void put(String alias, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(PREFIX + alias, originalUrl, Duration.ofHours(redirectTtlHours));
        } catch (Exception e) {
            log.warn("Cache write failed for alias {}: {}", alias, e.getMessage());
        }
    }

    public String get(String alias) {
        try {
            return redisTemplate.opsForValue().get(PREFIX + alias);
        } catch (Exception e) {
            log.warn("Cache read failed for alias {}: {}", alias, e.getMessage());
            return null;
        }
    }

    public void evict(String alias) {
        try {
            redisTemplate.delete(PREFIX + alias);
        } catch (Exception e) {
            log.warn("Cache eviction failed for alias {}: {}", alias, e.getMessage());
        }
    }
}
