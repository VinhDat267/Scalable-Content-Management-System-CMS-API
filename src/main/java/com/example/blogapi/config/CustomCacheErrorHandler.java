package com.example.blogapi.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom error handler for Redis cache operations.
 * Implements graceful degradation: when Redis is down,
 * the application continues to work normally by falling back to DB queries.
 * 
 * Based on: backend-architect skill → Resilience & Fault Tolerance → Graceful
 * Degradation
 */
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("⚠️ Cache GET failed - cache: '{}', key: '{}', error: {}. Falling back to DB.",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("⚠️ Cache PUT failed - cache: '{}', key: '{/}', error: {}. Data saved to DB only.",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("⚠️ Cache EVICT failed - cache: '{}', key: '{}', error: {}.",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("⚠️ Cache CLEAR failed - cache: '{}', error: {}.",
                cache.getName(), exception.getMessage());
    }
}
