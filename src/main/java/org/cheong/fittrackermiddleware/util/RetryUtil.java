package org.cheong.fittrackermiddleware.util;

import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.service.TokenService;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
public class RetryUtil {

    /**
     * Retry specifically for reactor code only
     * @param tokenService
     * @return
     */
    public static Retry retryOnUnauthorized(TokenService tokenService) {
        return Retry.from(retrySignal -> retrySignal
                .flatMap(signal -> {
                    if (signal.failure() instanceof WebSocketClientHandshakeException webSocketClientHandshakeException) {
                        if (webSocketClientHandshakeException.getMessage().contains(String.valueOf(HttpStatus.UNAUTHORIZED.value()))) {
                            log.error("Unauthorized. Evicting access token");
                            tokenService.evictAccessToken();
                            return Mono.just(signal);
                        }
                    }
                    return Mono.error(signal.failure());
                }));
    }
}
