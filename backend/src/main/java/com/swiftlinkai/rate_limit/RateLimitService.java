package com.swiftlinkai.rate_limit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.shorten-per-minute:100}")
    private int shortenLimit;

    @Value("${app.rate-limit.redirect-per-minute:1000}")
    private int redirectLimit;

    private static final String RL_PREFIX = "rate_limit:";

    public boolean allowShorten(String identifier) {
        return allow(RL_PREFIX + "shorten:" + identifier, shortenLimit, 60);
    }

    public boolean allowRedirect(String identifier) {
        return allow(RL_PREFIX + "redirect:" + identifier, redirectLimit, 60);
    }

    private boolean allow(String key, int limit, int windowSeconds) {
        try {
            long now = Instant.now().toEpochMilli();
            long windowStart = now - (windowSeconds * 1000L);

            // Sliding window using sorted set
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            Long count = redisTemplate.opsForZSet().zCard(key);

            if (count != null && count >= limit) {
                return false;
            }

            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds + 1));
            return true;
        } catch (Exception e) {
            log.warn("Rate limit check failed for key {}: {}", key, e.getMessage());
            return true; // Fail open on Redis errors
        }
    }
}
