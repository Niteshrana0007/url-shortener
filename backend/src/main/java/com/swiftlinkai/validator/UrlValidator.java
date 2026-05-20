package com.swiftlinkai.validator;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "localhost", "127.0.0.1", "0.0.0.0", "::1",
            "169.254.169.254" // AWS metadata endpoint
    );

    public boolean isValid(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) return false;
            if (!ALLOWED_SCHEMES.contains(scheme.toLowerCase())) return false;
            if (BLOCKED_DOMAINS.contains(host.toLowerCase())) return false;
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public boolean isSafeAlias(String alias) {
        if (alias == null) return false;
        return alias.matches("^[a-zA-Z0-9-_]{3,50}$");
    }
}
