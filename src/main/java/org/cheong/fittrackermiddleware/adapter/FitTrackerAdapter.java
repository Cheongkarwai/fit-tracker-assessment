package org.cheong.fittrackermiddleware.adapter;

import org.cheong.fittrackermiddleware.dto.ErrorMessage;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.dto.TokenRequest;
import org.cheong.fittrackermiddleware.dto.TokenResponse;
import org.cheong.fittrackermiddleware.exception.ApiException;
import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class FitTrackerAdapter extends AbstractHttpAdapter {

    private final FitTrackerProperties properties;

    public FitTrackerAdapter(@Qualifier("fitTrackerWebClient") WebClient webClient, FitTrackerProperties properties) {
        super(webClient, handle4xxError(), handle5xxError());
        this.properties = properties;
    }

    public Mono<HealthResponse> pingHealth() {
        return get("/api/health", null, HealthResponse.class, null);
    }

    public Mono<TokenResponse> getToken() {
        return post("/api/token", null, TokenResponse.class, null, new TokenRequest(properties.clientId(), properties.clientSecret()));
    }

    private static Function<ClientResponse, Mono<? extends Throwable>> handle4xxError() {
        return response -> {
            if (response.statusCode().is4xxClientError()) {
                return response.bodyToMono(ErrorMessage.class)
                        .map(errorMessage -> new ApiException(errorMessage.message()));
            }
            return Mono.empty();
        };
    }

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
