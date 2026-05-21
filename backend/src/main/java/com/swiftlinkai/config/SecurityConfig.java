package com.swiftlinkai.config;

import com.swiftlinkai.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/").permitAll() // <-- FIX 1: Allows your root URL to load without 403
                        .requestMatchers(HttpMethod.GET, "/link-*").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/{alias}").permitAll()
                        .requestMatchers("/actuator/**").permitAll() 
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        // Secured endpoints
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .contentTypeOptions(ct -> {})
                        .frameOptions(fo -> fo.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Dynamic Origin Configuration to support Vercel deployments
        List<String> allowedOrigins = new ArrayList<>();
        // allowedOrigins.add("http://localhost:*");
        // allowedOrigins.add("https://url-shortener-brown-seven.vercel.app/api/v1:*");
        // allowedOrigins.add("https://url-shortener-brown-seven.vercel.app:*");
        allowedOrigins.add("http://localhost:5173");
        allowedOrigins.add("https://url-shortener-brown-seven.vercel.app");
        allowedOrigins.add("https://url-shortener-h0irgxjhs-niteshrana754-9664s-projects.vercel.app");
        allowedOrigins.add("https://*.vercel.app"); // covers all preview deployments
    
        // Read your Vercel URL from Render Environment settings
        String prodFrontendUrl = System.getenv("APP_BASE_URL");
        if (prodFrontendUrl != null && !prodFrontendUrl.isEmpty()) {
            allowedOrigins.add(prodFrontendUrl);
        }

        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Combined headers from WebMvcConfig to be completely safe
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-Id", "X-Request-Id", "*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}