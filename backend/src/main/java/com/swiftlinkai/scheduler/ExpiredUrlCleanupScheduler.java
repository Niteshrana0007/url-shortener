package com.swiftlinkai.scheduler;

import com.swiftlinkai.cache.RedirectCacheService;
import com.swiftlinkai.entity.ShortUrl;
import com.swiftlinkai.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredUrlCleanupScheduler {

    private final ShortUrlRepository shortUrlRepository;
    private final RedirectCacheService redirectCacheService;

    @Scheduled(cron = "0 0 2 * * *") // Daily at 02:00
    @Transactional
    public void cleanupExpiredUrls() {
        log.info("Running expired URL cleanup job");
        int page = 0;
        int deactivated = 0;

        Page<ShortUrl> batch;
        do {
            batch = shortUrlRepository.findExpiredUrls(LocalDateTime.now(), PageRequest.of(page++, 500));
            for (ShortUrl url : batch) {
                url.setIsActive(false);
                redirectCacheService.evict(url.getAlias());
                deactivated++;
            }
            shortUrlRepository.saveAll(batch.getContent());
        } while (batch.hasNext());

        log.info("Expired URL cleanup complete. Deactivated: {}", deactivated);
    }
}
