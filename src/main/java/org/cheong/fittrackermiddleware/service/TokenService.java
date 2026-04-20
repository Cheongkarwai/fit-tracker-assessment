package org.cheong.fittrackermiddleware.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.cheong.fittrackermiddleware.dto.TokenResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author cheong
 * Service for token management
 */
@Service
@Slf4j
public class TokenService {

    private final FitTrackerAdapter fitTrackerAdapter;
    private final CacheManager cacheManager;

    /**
     * Constructor for TokenService
     * @param fitTrackerAdapter Fit Tracker Adapter for getting access token
     * @param cacheManager Cache Manager for caching access token
     */
    public TokenService(FitTrackerAdapter fitTrackerAdapter,
                        CacheManager cacheManager){
        this.fitTrackerAdapter = fitTrackerAdapter;
        this.cacheManager = cacheManager;
    }

    /**
     * Get access token from Redis cache or Fit Tracker server
     * @return Mono<TokenResponse>
     */
    @CircuitBreaker(name = "cache-client", fallbackMethod = "getCachedAccessTokenFallback")
    @Retry(name = "cache-client", fallbackMethod = "getCachedAccessTokenFallback")
    @Cacheable("access_token")
    public Mono<TokenResponse> getAccessToken(){
        return fitTrackerAdapter.getToken()
                .doOnNext(tokenResponse -> log.debug("Get access token={}", tokenResponse.accessToken()));
    }

    /**
     * Fallback method for getAccessToken when Redis server is unavailable
     * @param throwable Throwable
     * @return Mono<TokenResponse>
     */
    public Mono<TokenResponse> getCachedAccessTokenFallback(Throwable throwable){
        log.error("Redis server is unavailable", throwable);
        return fitTrackerAdapter.getToken()
                .doOnNext(tokenResponse -> log.debug("Get access token={}", tokenResponse.accessToken()));
    }

    /**
     * Evict access token from Redis cache
     */
    @CircuitBreaker(name = "cache-client", fallbackMethod = "evictAccessTokenFallback")
    @Retry(name = "cache-client", fallbackMethod = "evictAccessTokenFallback")
    public void evictAccessToken(){
        log.debug("Evicting access token");
        Objects.requireNonNull(cacheManager.getCache("access_token")).clear();
    }

    /**
     * Fallback method for evictAccessToken
     * @param throwable Throwable
     */
    public void evictAccessTokenFallback(Throwable throwable){
        log.error("Redis server is unavailable", throwable);
    }
}
