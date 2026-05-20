package com.swiftlinkai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftlinkai.dto.request.LoginRequest;
import com.swiftlinkai.dto.request.RegisterRequest;
import com.swiftlinkai.dto.request.ShortenUrlRequest;
import com.swiftlinkai.dto.response.AuthResponse;
import com.swiftlinkai.dto.response.ShortenUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UrlShorteningIntegrationTest extends BaseIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;

    private static String accessToken;
    private static String createdAlias;

    @Test
    @Order(1)
    @DisplayName("User can register and receive JWT")
    void registerUser() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("integration@test.com");
        req.setPassword("Test1234!");
        req.setTenantId("test-tenant");

        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/v1/auth/register", req, AuthResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getAccessToken()).isNotBlank();
        accessToken = resp.getBody().getAccessToken();
    }

    @Test
    @Order(2)
    @DisplayName("Authenticated user can shorten a URL")
    void shortenUrl() {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://example.com/integration-test-article");
        req.setCustomAlias("int-test");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ShortenUrlResponse> resp = restTemplate.exchange(
                "/api/v1/shorten",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                ShortenUrlResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getAlias()).isEqualTo("int-test");
        createdAlias = resp.getBody().getAlias();
    }

    @Test
    @Order(3)
    @DisplayName("Redirect returns 302 with Location header")
    void redirect() {
        ResponseEntity<Void> resp = restTemplate.exchange(
                "/api/v1/" + createdAlias,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(resp.getHeaders().getLocation()).isNotNull();
        assertThat(resp.getHeaders().getLocation().toString())
                .isEqualTo("https://example.com/integration-test-article");
    }

    @Test
    @Order(4)
    @DisplayName("Duplicate alias returns 409 Conflict")
    void duplicateAlias_conflict() {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://other.com");
        req.setCustomAlias("int-test");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/v1/shorten",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(5)
    @DisplayName("Unknown alias returns 404")
    void unknownAlias_notFound() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "/api/v1/nonexistent-alias-xyz", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
