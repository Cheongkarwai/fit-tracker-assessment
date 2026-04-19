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

@Service
@Slf4j
public class TokenService {

    private final FitTrackerAdapter fitTrackerAdapter;

    private final CacheManager cacheManager;

    public TokenService(FitTrackerAdapter fitTrackerAdapter,
                        CacheManager cacheManager){
        this.fitTrackerAdapter = fitTrackerAdapter;
        this.cacheManager = cacheManager;
    }

    @CircuitBreaker(name = "cache-client", fallbackMethod = "getCachedAccessTokenFallback")
    @Retry(name = "cache-client", fallbackMethod = "getCachedAccessTokenFallback")
    @Cacheable("access_token")
    public Mono<TokenResponse> getAccessToken(){
        return fitTrackerAdapter.getToken()
                .doOnNext(tokenResponse -> log.debug("Get access token={}", tokenResponse.accessToken()));
    }

    public Mono<TokenResponse> getCachedAccessTokenFallback(Throwable throwable){
        log.error("Redis server is unavailable", throwable);
        return fitTrackerAdapter.getToken()
                .doOnNext(tokenResponse -> log.debug("Get access token={}", tokenResponse.accessToken()));
    }

    public void evictAccessToken(){
        log.debug("Evicting access token");
        Objects.requireNonNull(cacheManager.getCache("access_token")).clear();
    }
}
