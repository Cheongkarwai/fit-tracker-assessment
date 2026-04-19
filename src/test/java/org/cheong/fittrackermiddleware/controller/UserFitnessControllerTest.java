package org.cheong.fittrackermiddleware.controller;

import io.r2dbc.spi.R2dbcException;
import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.exception.UserNotFoundException;
import org.cheong.fittrackermiddleware.service.UserFitnessService;
import org.cheong.fittrackermiddleware.service.UserTraceSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = UserFitnessController.class)
public class UserFitnessControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserFitnessService userFitnessService;

    @MockitoBean
    private UserTraceSubscriptionService userTraceSubscriptionService;

    @BeforeEach
    void setUp() {
        when(userTraceSubscriptionService.startSubscription()).thenReturn(Mono.empty());
    }

    @Test
    void getSpecificUserFitnessOnSpecificDate_return400HttpStatusCode_whenUserFitnessExistsAndRequestParameterDateIsMissing() {
        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").build(123))
                .exchange().expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class);
    }

    @Test
    void getSpecificUserFitnessOnSpecificDate_return404HttpStatusCode_whenUserNotExists() {

        UserNotFoundException userNotFoundException = new UserNotFoundException("User not found");
        when(userFitnessService.calculateUserFitness(any(), any())).thenReturn(Mono.error(userNotFoundException));

        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").queryParam("date", "2025-03-01").build(123))
                .exchange().expectStatus()
                .isNotFound()
                .expectBody(ProblemDetail.class);
    }

    @Test
    void getSpecificUserFitnessOnSpecificDate_return500HttpStatusCode_whenDatabaseOperationFails() {

        when(userFitnessService.calculateUserFitness(any(), any())).thenReturn(Mono.error(new R2dbcException() {
            @Override
            public String getMessage() {
                return "Database error";
            }
        }));

        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").queryParam("date", "2025-03-01").build(123))
                .exchange().expectStatus()
                .is5xxServerError()
                .expectBody(ProblemDetail.class);
    }

    @Test
    void getSpecificUserFitnessOnSpecificDate_return200HttpStatusCode_whenUserFitnessExists() {

        when(userFitnessService.calculateUserFitness(any(), any())).thenReturn(Mono.just(new UserFitnessDTO(1, 2.0, 2.0, 2.0)));

        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").queryParam("date", "2025-03-01").build(123))
                .exchange().expectStatus()
                .isOk();
    }
}
