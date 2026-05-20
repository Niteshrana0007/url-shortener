package com.swiftlinkai.repository;

import com.swiftlinkai.entity.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByAlias(String alias);

    Optional<ShortUrl> findByAliasAndTenantId(String alias, String tenantId);

    boolean existsByAlias(String alias);

    Page<ShortUrl> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);

    @Query("SELECT s FROM ShortUrl s WHERE s.tenantId = :tenantId AND s.isActive = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now) ORDER BY s.createdAt DESC")
    Page<ShortUrl> findActiveByTenantId(@Param("tenantId") String tenantId,
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.id = :id")
    void incrementClickCount(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM ShortUrl s WHERE s.tenantId = :tenantId AND s.isActive = true")
    long countActiveByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT s FROM ShortUrl s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :now AND s.isActive = true")
    Page<ShortUrl> findExpiredUrls(@Param("now") LocalDateTime now, Pageable pageable);
}
