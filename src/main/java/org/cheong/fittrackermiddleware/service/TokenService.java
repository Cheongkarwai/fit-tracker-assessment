package org.cheong.fittrackermiddleware.service;

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

    @Cacheable("access_token")
    public Mono<TokenResponse> getAccessToken(){
        return fitTrackerAdapter.getToken();
    }

    public void evictAccessToken(){
        log.debug("Evicting access token");
        Objects.requireNonNull(cacheManager.getCache("access_token")).clear();
    }
}
