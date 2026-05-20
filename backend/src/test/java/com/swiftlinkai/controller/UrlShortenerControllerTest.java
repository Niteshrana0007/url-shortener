package com.swiftlinkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftlinkai.dto.request.ShortenUrlRequest;
import com.swiftlinkai.dto.response.ShortenUrlResponse;
import com.swiftlinkai.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UrlShortenerService urlShortenerService;

    @Test
    @WithMockUser(roles = "USER")
    void shorten_validRequest_returns201() throws Exception {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://example.com/test-article");

        ShortenUrlResponse resp = ShortenUrlResponse.builder()
                .alias("test-article").shortUrl("https://swiftlink.ai/test-article")
                .tags(List.of("Tech")).category("Technology").aiGenerated(true).build();

        when(urlShortenerService.shorten(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alias").value("test-article"))
                .andExpect(jsonPath("$.aiGenerated").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shorten_invalidUrl_returns400() throws Exception {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("not-a-url");

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.longUrl").exists());
    }

    @Test
    void shorten_unauthenticated_returns401() throws Exception {
        ShortenUrlRequest req = new ShortenUrlRequest();
        req.setLongUrl("https://example.com");

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
