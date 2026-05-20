package com.swiftlinkai.service;

import com.swiftlinkai.dto.request.ShortenUrlRequest;
import com.swiftlinkai.dto.response.ShortenUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlShortenerService {

    ShortenUrlResponse shorten(ShortenUrlRequest request, String userId, String tenantId);

    String resolveAlias(String alias);

    Page<ShortenUrlResponse> listUrls(String tenantId, Pageable pageable);

    void deactivateUrl(String alias, String tenantId);

    ShortenUrlResponse getUrlDetails(String alias, String tenantId);
}
