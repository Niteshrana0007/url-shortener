package com.swiftlinkai.integration;

import com.swiftlinkai.dto.request.LoginRequest;
import com.swiftlinkai.dto.request.RegisterRequest;
import com.swiftlinkai.dto.response.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Login with wrong password returns 401")
    void login_wrongPassword_returns401() {
        // First register
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("auth-test@example.com");
        reg.setPassword("Correct1234!");
        reg.setTenantId("auth-tenant");
        restTemplate.postForEntity("/api/v1/auth/register", reg, AuthResponse.class);

        // Then try wrong password
        LoginRequest login = new LoginRequest();
        login.setEmail("auth-test@example.com");
        login.setPassword("WrongPassword!");

        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/v1/auth/login", login, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Duplicate email registration returns 409")
    void register_duplicateEmail_returns409() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("duplicate@example.com");
        req.setPassword("Pass1234!");
        req.setTenantId("dup-tenant");

        restTemplate.postForEntity("/api/v1/auth/register", req, AuthResponse.class);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/v1/auth/register", req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Protected endpoint without token returns 401")
    void protectedEndpoint_noToken_returns401() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/v1/urls", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
