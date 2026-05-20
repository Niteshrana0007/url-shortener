package com.swiftlinkai.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    // Base64 of 32 random bytes (256-bit key)
    private static final String SECRET =
            "c3dpZnRsaW5rYWlzdXBlcnNlY3JldGtleWZvcmp3dHRva2VucHJvZHVjdGlvbg==";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 900_000L, 604_800_000L);
    }

    @Test
    @DisplayName("Generated access token is valid and contains correct claims")
    void generateAndValidate_accessToken() {
        String token = provider.generateAccessToken("42", "user@test.com", "tenant-1", "USER");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo("42");
        assertThat(provider.getTenantId(token)).isEqualTo("tenant-1");
        assertThat(provider.getRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void tamperedToken_failsValidation() {
        String token = provider.generateAccessToken("1", "x@x.com", "t", "USER");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("Expired token fails validation")
    void expiredToken_failsValidation() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken("1", "x@x.com", "t", "USER");

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThat(shortLived.validateToken(token)).isFalse();
    }
}
