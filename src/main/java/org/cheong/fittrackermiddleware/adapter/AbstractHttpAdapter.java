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

/**
 * @author cheong
 * Abstract class for HTTP adapter
 */
public abstract class AbstractHttpAdapter {

    /**
     * WebClient for HTTP request
     */
    protected WebClient webClient;
    /**
     * Function for handling 4xx error
     */
    protected Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction4xx;
    /**
     * Function for handling 5xx error
     */
    protected Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction5xx;

    /**
     * Constructor for AbstractHttpAdapter
     * @param webClient WebClient for HTTP request
     * @param errorHandlingFunction4xx Function for handling 4xx error
     * @param errorHandlingFunction5xx Function for handling 5xx error
     */
    public AbstractHttpAdapter(WebClient webClient, Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction4xx, Function<ClientResponse, Mono<? extends Throwable>> errorHandlingFunction5xx) {
        this.webClient = webClient;
        this.errorHandlingFunction4xx = errorHandlingFunction4xx;
        this.errorHandlingFunction5xx = errorHandlingFunction5xx;
    }

    /**
     * Perform HTTP GET request
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param responseType Response type for the request
     * @param pathVariables Path variables for the request
     * @return Mono of response type
     * @param <R> Response type
     */
    protected final <R> Mono<R> get(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.GET, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }

    /**
     * Perform HTTP GET request for all records
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param responseType Response type for the request
     * @param pathVariables Path variables for the request
     * @return Flux of response type
     * @param <R> Response type
     */
    protected final <R> Flux<R> getAll(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.GET, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToFlux(responseType);
    }

    /**
     * Perform HTTP POST request for all records
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param responseType Response type for the request
     * @param pathVariables Path variables for the request
     * @param body Body for the request
     * @return Mono of response type
     * @param <R> Response type
     */
    protected final <R> Mono<R> post(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables, Object body) {
        return getRequestHeadersSpec(HttpMethod.POST, uri, queryParams, pathVariables, body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }


    /**
     * Perform HTTP DELETE request for all records
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param responseType Response type for the request
     * @param pathVariables Path variables for the request
     * @return Mono of response type
     * @param <R> Response type
     */
    protected final <R> Mono<R> delete(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables) {
        return getRequestHeadersSpec(HttpMethod.DELETE, uri, queryParams, pathVariables, null)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }


    /**
     * Perform HTTP UPDATE request for all records
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param responseType Response type for the request
     * @param pathVariables Path variables for the request
     * @param body Body for the request
     * @return Flux of response type
     * @param <R> Response type
     */
    protected final <R> Mono<R> update(String uri, MultiValueMap<String, String> queryParams, Class<R> responseType, List<Object> pathVariables, Object body) {
        return getRequestHeadersSpec(HttpMethod.PUT, uri, queryParams, pathVariables, body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, errorHandlingFunction4xx)
                .onStatus(HttpStatusCode::is5xxServerError, errorHandlingFunction5xx)
                .bodyToMono(responseType);
    }


    /**
     * Get request headers specification for the given HTTP method, URI, query parameters, path variables, and body
     * @param httpMethod HTTP method for the request
     * @param uri URI for the request
     * @param queryParams Query parameters for the request
     * @param pathVariables Path variables for the request
     * @param body Body for the request
     * @return Request headers specification
     */
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
