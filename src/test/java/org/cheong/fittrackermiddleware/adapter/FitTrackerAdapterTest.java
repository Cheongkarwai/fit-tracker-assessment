package org.cheong.fittrackermiddleware.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.cheong.fittrackermiddleware.dto.ErrorMessage;
import org.cheong.fittrackermiddleware.dto.HealthResponse;
import org.cheong.fittrackermiddleware.dto.TokenResponse;
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

public class FitTrackerAdapterTest {

    private FitTrackerAdapter fitTrackerAdapter;
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
        fitTrackerAdapter = new FitTrackerAdapter(webClient, properties);
    }

    @Test
    @DisplayName("Ping health endpoint should return health response when server returns 2xx")
    void pingHealth_shouldReturnHealthResponse_whenServerReturns2xx() throws JsonProcessingException, InterruptedException {
        HealthResponse healthResponse = new HealthResponse(true);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(healthResponse)));

        StepVerifier.create(fitTrackerAdapter.pingHealth())
                .expectNextMatches(response -> response.equals(healthResponse))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/health");
    }

    @Test
    @DisplayName("Ping health endpoint should throw ApiException when server returns 5xx")
    void pingHealth_shouldThrowApiException_whenServerReturns5xx() throws JsonProcessingException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(new ErrorMessage("Server is not up"))));

        StepVerifier.create(fitTrackerAdapter.pingHealth())
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/health");
    }

    @Test
    @DisplayName("Ping health endpoint should throw ApiException when server returns 4xx")
    void pingHealth_shouldThrowApiException_whenServerReturns4xx() throws JsonProcessingException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(new ErrorMessage("Server is not up"))));

        StepVerifier.create(fitTrackerAdapter.pingHealth())
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/health");
    }

    @Test
    @DisplayName("Get access token endpoint should return access token when server returns 2xx")
    void getAccessToken_shouldThrowApiException_whenServerReturns2xx() throws JsonProcessingException, InterruptedException {
        TokenResponse tokenResponse = new TokenResponse("some-token");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(tokenResponse))
        );

        StepVerifier.create(fitTrackerAdapter.getToken())
                .expectNextMatches(response -> tokenResponse.accessToken().equals(response.accessToken()))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/token");
    }

    @Test
    @DisplayName("Get access token endpoint should throw ApiException when server returns 4xx")
    void getAccessToken_shouldThrowApiException_whenServerReturns4xx() throws JsonProcessingException, InterruptedException {
        TokenResponse tokenResponse = new TokenResponse("some-token");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(tokenResponse))
        );

        StepVerifier.create(fitTrackerAdapter.getToken())
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/token");
    }

    @Test
    @DisplayName("Get access token endpoint should throw ApiException when server returns 5xx")
    void getAccessToken_shouldThrowApiException_whenServerReturns5xx() throws JsonProcessingException, InterruptedException {
        TokenResponse tokenResponse = new TokenResponse("some-token");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(tokenResponse))
        );

        StepVerifier.create(fitTrackerAdapter.getToken())
                .expectErrorMatches(throwable -> throwable instanceof ApiException)
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/token");
    }
}
