package org.cheong.fittrackermiddleware.adapter;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.dto.ErrorMessage;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.dto.TokenRequest;
import org.cheong.fittrackermiddleware.dto.TokenResponse;
import org.cheong.fittrackermiddleware.exception.ApiException;
import org.cheong.fittrackermiddleware.exception.FitTrackerServerUnavailable;
import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author cheong
 * Adapter for public endpoint Fit Tracker server
 */
@Component
@Slf4j
public class FitTrackerAdapter extends AbstractHttpAdapter {

    private final FitTrackerProperties properties;

    /**
     * Constructor for FitTrackerAdapter
     * @param webClient webClient for HTTP request
     * @param properties properties for Fit Tracker server
     */
    public FitTrackerAdapter(@Qualifier("fitTrackerWebClient") WebClient webClient, FitTrackerProperties properties) {
        super(webClient, handle4xxError(), handle5xxError());
        this.properties = properties;
    }

    /**
     * Ping Fit Tracker server to check if server is available
     * @return Mono<HealthResponse>
     */
    @Retry(name = "fit-tracker-client", fallbackMethod = "pingFitTrackerServerFallBack")
    @Bulkhead(name = "fit-tracker-client", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "pingFitTrackerServerFallBack")
    @CircuitBreaker(name = "fit-tracker-client", fallbackMethod = "pingFitTrackerServerFallBack")
    public Mono<HealthResponse> pingHealth() {
        return get("/api/health", null, HealthResponse.class, null)
                .filter(HealthResponse::isUp)
                .switchIfEmpty(Mono.error(new FitTrackerServerUnavailable("Fit Tracker server is unavailable.")));
    }

    /**
     * Fallback method for pingFitTrackerServer
     * @param throwable Throwable thrown by pingFitTrackerServer
     * @return Mono<HealthResponse>
     */
    public Mono<HealthResponse> pingFitTrackerServerFallBack(Throwable throwable) {
        log.error("Pinging Fit Tracker Server. Fit Tracker Server is unavailable", throwable);
        return Mono.error(new FitTrackerServerUnavailable("Fit Tracker server is unavailable."));
    }

    /**
     * Get access token from Fit Tracker server
     * @return Mono<TokenResponse>
     */
    @Retry(name = "fit-tracker-client", fallbackMethod = "getTokenFallback")
    @Bulkhead(name = "fit-tracker-client", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getTokenFallback")
    @CircuitBreaker(name = "fit-tracker-client", fallbackMethod = "getTokenFallback")
    public Mono<TokenResponse> getToken() {
        return post("/api/token", null, TokenResponse.class, null, new TokenRequest(properties.clientId(), properties.clientSecret()));
    }

    /**
     * Fallback method for getToken
     * @param throwable Throwable thrown by getToken
     * @return Mono<TokenResponse>
     */
    public Mono<TokenResponse> getTokenFallback(Throwable throwable) {
        log.error("Getting access token. Fit Tracker Server is unavailable", throwable);
        return Mono.error(new FitTrackerServerUnavailable("Fit Tracker server is unavailable."));
    }

    /**
     * Handle 4xx errors
     * @return Function<ClientResponse, Mono<? extends Throwable>>
     */
    private static Function<ClientResponse, Mono<? extends Throwable>> handle4xxError() {
        return response -> {
            if (response.statusCode().is4xxClientError()) {
                return response.bodyToMono(ErrorMessage.class)
                        .map(errorMessage -> new ApiException(errorMessage.message()));
            }
            return Mono.empty();
        };
    }

    /**
     * Handle 5xx errors
     * @return Function<ClientResponse, Mono<? extends Throwable>>
     */
    private static Function<ClientResponse, Mono<? extends Throwable>> handle5xxError() {
        return response -> {
            if (response.statusCode().is5xxServerError()) {
                return response.bodyToMono(ErrorMessage.class)
                        .map(errorMessage -> new ApiException(errorMessage.message()));
            }
            return Mono.empty();
        };
    }
}
