package org.cheong.fittrackermiddleware.configuration;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.cheong.fittrackermiddleware.service.TokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties(FitTrackerProperties.class)
@EnableScheduling
@Slf4j
public class WebConfiguration {

    @Bean
    public WebClient.Builder baseWebClientBuilder(FitTrackerProperties properties,
                                                  @Qualifier("logRequestFilterFunction") ExchangeFilterFunction logRequestFilterFunction,
                                                  @Qualifier("logResponseFilterFunction") ExchangeFilterFunction logResponseFilterFunction) {
        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apiVersionInserter(ApiVersionInserter.builder().usePathSegment(1).build())
                .filters(filterFunctions -> {
                    filterFunctions.add(logRequestFilterFunction);
                    filterFunctions.add(logResponseFilterFunction);
                })
                .defaultApiVersion(properties.version());
    }

    @Bean
    public WebClient fitTrackerWebClient(WebClient.Builder builder) {
        return builder
                .build();
    }

    @Bean
    public WebClient fitTrackerAuthorizedWebClient(WebClient.Builder builder,
                                                   @Qualifier("accessTokenFilterFunction") ExchangeFilterFunction exchangeFilterFunction) {
        return builder
                .filter(exchangeFilterFunction)
                .build();
    }

    @Bean
    public ExchangeFilterFunction accessTokenFilterFunction(TokenService tokenService) {

        return (request, next) -> {
            return tokenService.getAccessToken()
                    .doOnNext(tokenResponse -> log.debug("Retrieving access token={}", tokenResponse.accessToken()))
                    .flatMap(tokenResponse -> next.exchange(ClientRequest.from(request).headers(httpHeaders ->
                            httpHeaders.setBearerAuth(tokenResponse.accessToken())).build()))
                    .flatMap(clientResponse -> {
                        if (HttpStatus.UNAUTHORIZED.value() == clientResponse.statusCode().value()) {
                            tokenService.evictAccessToken();
                            return tokenService.getAccessToken()
                                    .flatMap(tokenResponse ->
                                            next.exchange(ClientRequest.from(request)
                                                    .headers(httpHeaders ->
                                                            httpHeaders.setBearerAuth(tokenResponse.accessToken()))
                                                    .build()));
                        }
                        return Mono.just(clientResponse);
                    });
        };
    }

    @Bean
    public ExchangeFilterFunction logRequestFilterFunction() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            clientRequest.headers()
                    .forEach((name, values) -> log.debug("Request Headers Key={}, Value={}", name, values));
            log.debug("Request Method={}, Request Url={}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    @Bean
    public ExchangeFilterFunction logResponseFilterFunction() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            clientResponse.headers()
                    .asHttpHeaders()
                    .forEach((name, values) -> log.debug("Response Headers Key={}, Value={}", name, values));
            log.debug("Response Status={}, Response Url={}", clientResponse.statusCode(), clientResponse.request().getURI());
            return Mono.just(clientResponse);
        });
    }
}
