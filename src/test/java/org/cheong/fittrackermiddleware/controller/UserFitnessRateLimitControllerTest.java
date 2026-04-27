package org.cheong.fittrackermiddleware.controller;

import org.cheong.fittrackermiddleware.dto.UserFitnessDTO;
import org.cheong.fittrackermiddleware.service.UserFitnessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "resilience4j.ratelimiter.instances.user-fitness-api.limit-for-period=1",
        "resilience4j.ratelimiter.instances.user-fitness-api.limit-refresh-period=60s",
        "resilience4j.ratelimiter.instances.user-fitness-api.timeout-duration=0ms"
})
public class UserFitnessRateLimitControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserFitnessService userFitnessService;


    @Test
    void getSpecificUserFitnessOnSpecificDate_return429HttpStatusCode_whenRateLimitExceeded() {

        when(userFitnessService.calculateUserFitness(any(), any())).thenReturn(Mono.just(new UserFitnessDTO(1, 2.0, 2.0, 2.0)));

        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").queryParam("date", "2025-03-01").build(123))
                .exchange().expectStatus()
                .isOk();

        webTestClient
                .get().uri(uriBuilder -> uriBuilder.path("/api/v1/users/{userId}/fitness").queryParam("date", "2025-03-01").build(123))
                .exchange().expectStatus()
                .isEqualTo(429)
                .expectBody(ProblemDetail.class);
    }
}
