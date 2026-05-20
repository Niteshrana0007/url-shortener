package com.swiftlinkai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_analytics", indexes = {
    @Index(name = "idx_analytics_short_url", columnList = "short_url_id"),
    @Index(name = "idx_analytics_tenant", columnList = "tenant_id"),
    @Index(name = "idx_analytics_accessed_at", columnList = "accessed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id")
    private ShortUrl shortUrl;

    @Column(name = "ip_address", length = 255)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String referer;

    @Column(length = 255)
    private String country;

    @Column(length = 255)
    private String city;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @CreationTimestamp
    @Column(name = "accessed_at", updatable = false)
    private LocalDateTime accessedAt;
}
