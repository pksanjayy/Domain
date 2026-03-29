package com.hyundai.dms.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Utility service for manual cache eviction across modules.
 * Can be injected wherever programmatic cache control is needed.
 */
@Slf4j
@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evicts a single key from the specified cache.
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("Evicted key '{}' from cache '{}'", key, cacheName);
        } else {
            log.warn("Cache '{}' not found for eviction", cacheName);
        }
    }

    /**
     * Clears all entries from the specified cache.
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache '{}'", cacheName);
        } else {
            log.warn("Cache '{}' not found for clearing", cacheName);
        }
    }

    /**
     * Clears all known caches.
     */
    public void clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String name : cacheNames) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("Cleared all caches: {}", cacheNames);
    }
}
