package org.cheong.fittrackermiddleware.adapter;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractHttpAdapter {

    protected WebClient webClient;
    protected Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction4xx;
    protected Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction5xx;

    public AbstractHttpAdapter(WebClient webClient, Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction4xx, Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction5xx) {
        this.webClient = webClient;
        this.errorHandlingFunction4xx = errorHandlingFunction4xx;
        this.errorHandlingFunction5xx = errorHandlingFunction5xx;
    }

    protected final <R> Mono<R> get(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.GET, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }

    protected final <R> Flux<R> getAll(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.GET, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToFlux(responseType);
    }

    protected final <R> Mono<R> post(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables, Object body) {
        return getRequestHeadersSpec(HttpMethod.POST, uri, queryParams, pathVariables, body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }

    protected final <R> Mono<R> delete(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.DELETE, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }

    protected final <R> Mono<R> update(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables, Object body) {
        return getRequestHeadersSpec(HttpMethod.PUT, uri, queryParams, pathVariables, body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }


    private WebClient.RequestHeadersSpec<?> getRequestHeadersSpec(HttpMethod httpMethod, String uri, MultiValueMap<String, String> queryParams, List<Object> pathVariables, Object body) {
        var requestSpec = this.webClient
                .method(httpMethod)
                .uri(uri, uriBuilder -> {
                    uriBuilder.queryParams(queryParams);
                    if (CollectionUtils.isEmpty(pathVariables)) {
                        return uriBuilder.build();
                    }
                    return uriBuilder.build(pathVariables.toArray());
                });

        if (body != null) {
            return requestSpec.bodyValue(body);
        }

        return requestSpec;
    }

}
