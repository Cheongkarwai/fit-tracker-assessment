package org.cheong.fittrackermiddleware.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.cheong.fittrackermiddleware.exception.ApiException;
import org.cheong.fittrackermiddleware.factory.FitTrackerPropertiesFactory;
import org.cheong.fittrackermiddleware.factory.WebClientTestFactory;
import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorizedFitTrackerAdapterTest {

    private AuthorizedFitTrackerAdapter authorizedFitTrackerAdapter;
    private static MockWebServer mockWebServer;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void cleanUp() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        FitTrackerProperties properties = FitTrackerPropertiesFactory.createFitTrackerProperties(mockWebServer.url("/").toString());
        WebClient webClient = WebClientTestFactory.createWebClient(properties);
        authorizedFitTrackerAdapter = new AuthorizedFitTrackerAdapter(webClient);
    }

    @Test
    @DisplayName("Get user endpoint should return user response when server returns 2xx")
    void getUser_shouldReturnUserResponse_whenServerReturns2xx() throws JsonProcessingException, InterruptedException {
        UserResponse userResponse = new UserResponse("123", 5, 10);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(userResponse)));

        StepVerifier.create(authorizedFitTrackerAdapter.getUser("123"))
                .assertNext(response -> assertEquals(userResponse.id(), response.id()))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/users/123");
    }

    @Test
    @DisplayName("Get user endpoint should throw ApiException when server returns 4xx")
    void getUser_shouldThrowApiException_whenServerReturns4xx() throws JsonProcessingException, InterruptedException {
        ApiException apiException = new ApiException("Server returned 4xx");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(apiException)));

        StepVerifier.create(authorizedFitTrackerAdapter.getUser("123"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/users/123");
    }

    @Test
    @DisplayName("Get user endpoint should throw ApiException when server returns 5xx")
    void getUser_shouldThrowApiException_whenServerReturns5xx() throws JsonProcessingException, InterruptedException {
        ApiException apiException = new ApiException("Server returned 5xx");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(apiException)));

        StepVerifier.create(authorizedFitTrackerAdapter.getUser("123"))
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/users/123");
    }
}
