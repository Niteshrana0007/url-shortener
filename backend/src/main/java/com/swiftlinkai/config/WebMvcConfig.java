package com.swiftlinkai.config;

import com.swiftlinkai.security.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
        "/api/v1/auth/**", 
                    "/api/v1/**"
                );// Add the path used for public redirects here);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Grab the Vercel URL from your Render environment variables
        String frontendUrl = System.getenv("APP_BASE_URL");
        
        // Fallback to local dev URL if Render environment variable isn't found
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173"; 
        }

        registry.addMapping("/**")
                .allowedOrigins(frontendUrl, "https://url-shortener-qs8ksw5fe-niteshrana754-9664s-projects.vercel.app", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}