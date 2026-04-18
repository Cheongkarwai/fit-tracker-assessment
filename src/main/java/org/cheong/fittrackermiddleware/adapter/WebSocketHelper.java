package org.cheong.fittrackermiddleware.adapter;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

import static org.cheong.fittrackermiddleware.util.RetryUtil.retryOnUnauthorized;

@Slf4j
@Component
public class WebSocketHelper {

    private final WebSocketClient webSocketClient;

    private final TokenService tokenService;

    protected WebSocketHelper(WebSocketClient webSocketClient,
                              TokenService tokenService) {
        this.webSocketClient = webSocketClient;
        this.tokenService = tokenService;
    }

    protected final Flux<String> receive(URI path) {
        Sinks.Many<String> sinks = Sinks.many().multicast().onBackpressureBuffer();

        tokenService.getAccessToken()
                .map(accessToken -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setBearerAuth(accessToken.accessToken());
                    return httpHeaders;
                })
                .flatMap(httpHeaders -> webSocketClient
                        .execute(path,
                                httpHeaders,
                                session -> session.receive().map(WebSocketMessage::getPayloadAsText)
                                        .doOnNext(sinks::tryEmitNext)
                                        .doOnError(sinks::tryEmitError)
                                        .doOnComplete(sinks::tryEmitComplete)
                                        .then())
                        .retryWhen(retryOnUnauthorized(tokenService)))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return sinks.asFlux();

    }
}
