package org.cheong.fittrackermiddleware.service;

import org.cheong.fittrackermiddleware.adapter.FitTrackerAdapter;
import org.cheong.fittrackermiddleware.configuration.CacheConfiguration;
import org.cheong.fittrackermiddleware.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CacheConfiguration.class, TokenService.class})
public class TokenServiceTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private FitTrackerAdapter fitTrackerAdapter;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("access_token").clear();
    }

    @Test
    @DisplayName("Get access token for the first time will cache miss. For subsequent requests will cache hit")
    void getAccessTokenForFirstTime_shouldCacheMiss_forSubsequentRequests_shouldCacheHit() {
        TokenResponse tokenResponse = new TokenResponse("123");
        when(fitTrackerAdapter.getToken()).thenReturn(Mono.just(tokenResponse));

        StepVerifier.create(tokenService.getAccessToken())
                .expectNextMatches(response -> response.accessToken().equals(tokenResponse.accessToken()))
                .verifyComplete();

        verify(fitTrackerAdapter, times(1)).getToken();

        StepVerifier.create(tokenService.getAccessToken())
                .expectNextMatches(response -> response.accessToken().equals(tokenResponse.accessToken()))
                .verifyComplete();

        verifyNoMoreInteractions(fitTrackerAdapter);
    }
}
