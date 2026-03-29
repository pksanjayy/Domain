package com.hyundai.dms.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Multi-tier caching configuration.
 * Tier 1 — Caffeine (in-process, fast) with per-cache TTLs.
 */
@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(CacheManager redisCacheManager) {
        CompositeCacheManager composite = new CompositeCacheManager();
        composite.setCacheManagers(java.util.List.of(
                caffeineCacheManager(),
                redisCacheManager
        ));
        composite.setFallbackToNoOpCache(false);
        return composite;
    }

    @Bean
    public CacheManager caffeineCacheManager() {
        // Individual cache managers with different TTLs
        CaffeineCacheManager codesCache = buildCaffeineCacheManager("codes", 60, 1000);
        CaffeineCacheManager rolesCache = buildCaffeineCacheManager("roles", 5, 200);
        CaffeineCacheManager menusCache = buildCaffeineCacheManager("menus", 30, 500);
        CaffeineCacheManager branchesCache = buildCaffeineCacheManager("branches", 60, 500);
        CaffeineCacheManager vehicleModelsCache = buildCaffeineCacheManager("vehicleModels", 30, 500);

        // Composite of all Caffeine caches
        CompositeCacheManager caffeine = new CompositeCacheManager();
        caffeine.setCacheManagers(java.util.List.of(
                codesCache, rolesCache, menusCache, branchesCache, vehicleModelsCache
        ));
        caffeine.setFallbackToNoOpCache(false);
        return caffeine;
    }

    private CaffeineCacheManager buildCaffeineCacheManager(String cacheName, long ttlMinutes, long maxSize) {
        CaffeineCacheManager manager = new CaffeineCacheManager(cacheName);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
