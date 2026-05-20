package com.swiftlinkai.repository;

import com.swiftlinkai.entity.UrlAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlAnalyticsRepository extends JpaRepository<UrlAnalytics, Long> {

    Page<UrlAnalytics> findByShortUrlIdAndTenantId(Long shortUrlId, String tenantId, Pageable pageable);

    @Query("SELECT a.country, COUNT(a) as hits FROM UrlAnalytics a " +
           "WHERE a.shortUrl.id = :shortUrlId AND a.tenantId = :tenantId " +
           "AND a.accessedAt >= :since GROUP BY a.country ORDER BY hits DESC")
    List<Object[]> countHitsByCountry(@Param("shortUrlId") Long shortUrlId,
                                       @Param("tenantId") String tenantId,
                                       @Param("since") LocalDateTime since);

    @Query("SELECT DATE(a.accessedAt), COUNT(a) FROM UrlAnalytics a " +
           "WHERE a.shortUrl.id = :shortUrlId AND a.tenantId = :tenantId " +
           "AND a.accessedAt >= :since GROUP BY DATE(a.accessedAt) ORDER BY DATE(a.accessedAt)")
    List<Object[]> countDailyHits(@Param("shortUrlId") Long shortUrlId,
                                   @Param("tenantId") String tenantId,
                                   @Param("since") LocalDateTime since);

    long countByShortUrlIdAndTenantId(Long shortUrlId, String tenantId);
}
