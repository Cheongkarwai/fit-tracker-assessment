package org.cheong.fittrackermiddleware.adapter;

import org.cheong.fittrackermiddleware.dto.ErrorMessage;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.cheong.fittrackermiddleware.exception.ApiException;
import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
public class AuthorizedFitTrackerAdapter extends AbstractHttpAdapter {

    public AuthorizedFitTrackerAdapter(@Qualifier("fitTrackerAuthorizedWebClient") WebClient webClient) {
        super(webClient, handle4xxError(), handle5xxError());
    }

    public Mono<UserResponse> getUser(String userId) {
        return get("/api/users/{userId}", null, UserResponse.class, List.of(userId));
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
