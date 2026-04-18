package org.cheong.fittrackermiddleware.util;

import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.service.TokenService;
import org.springframework.http.HttpStatus;
import reactor.util.retry.Retry;
import java.time.Duration;

@Slf4j
public class RetryUtil {

    /**
     * Retry specifically for reactor code only
     * @param tokenService
     * @return
     */
    public static Retry retryOnUnauthorized(TokenService tokenService) {
        return Retry.backoff(100, Duration.ofMinutes(1))
                .filter(throwable -> throwable instanceof WebSocketClientHandshakeException ws
                        && ws.getMessage().contains(String.valueOf(HttpStatus.UNAUTHORIZED.value())))
                .doBeforeRetry(retrySignal -> {
                    log.warn("Unauthorized. Evicting access token before retry #{}", retrySignal.totalRetries());
                    tokenService.evictAccessToken();
                })
                .doAfterRetry(retrySignal -> log.error("Retrying after {} seconds", retrySignal.totalRetries()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("Retry exhausted, error={}", retrySignal.failure().getMessage(), retrySignal.failure());
                    return retrySignal.failure();
                });
    }

}
