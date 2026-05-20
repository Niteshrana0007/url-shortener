package com.swiftlinkai.rate_limit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ZSetOperations<String, String> zSetOps;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(redisTemplate);
        ReflectionTestUtils.setField(rateLimitService, "shortenLimit", 100);
        ReflectionTestUtils.setField(rateLimitService, "redirectLimit", 1000);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    @DisplayName("allowShorten() returns true when under limit")
    void allowShorten_underLimit() {
        when(zSetOps.removeRangeByScore(any(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOps.zCard(any())).thenReturn(5L);
        when(zSetOps.add(any(), any(), anyDouble())).thenReturn(true);

        assertThat(rateLimitService.allowShorten("127.0.0.1")).isTrue();
    }

    @Test
    @DisplayName("allowShorten() returns false when limit reached")
    void allowShorten_limitReached() {
        when(zSetOps.removeRangeByScore(any(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOps.zCard(any())).thenReturn(100L);

        assertThat(rateLimitService.allowShorten("127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("allowShorten() fails open when Redis is unavailable")
    void allowShorten_redisFailsOpen() {
        when(zSetOps.removeRangeByScore(any(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Redis unavailable"));

        assertThat(rateLimitService.allowShorten("127.0.0.1")).isTrue();
    }
}
