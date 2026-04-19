package org.cheong.fittrackermiddleware.adapter;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.dto.ErrorMessage;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.cheong.fittrackermiddleware.exception.ApiException;
import org.cheong.fittrackermiddleware.exception.FitTrackerServerUnavailable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class AuthorizedFitTrackerAdapter extends AbstractHttpAdapter {

    public AuthorizedFitTrackerAdapter(@Qualifier("fitTrackerAuthorizedWebClient") WebClient webClient) {
        super(webClient, handle4xxError(), handle5xxError());
    }

    @Retry(name = "fit-tracker-client", fallbackMethod = "getUserFallback")
    @Bulkhead(name = "fit-tracker-client", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getUserFallback")
    @CircuitBreaker(name = "fit-tracker-client", fallbackMethod = "getUserFallback")
    public Mono<UserResponse> getUser(String userId) {
        return get("/api/users/{userId}", null, UserResponse.class, List.of(userId));
    }

    public Mono<UserResponse> getUserFallback(String userId, Throwable throwable) {
        log.error("Retrieving userId={}, Fit Tracker Server is unavailable", userId, throwable);
        return Mono.error(new FitTrackerServerUnavailable("Fit Tracker server is unavailable."));
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
