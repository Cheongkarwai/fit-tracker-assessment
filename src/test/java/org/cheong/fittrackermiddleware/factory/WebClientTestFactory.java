package org.cheong.fittrackermiddleware.factory;

import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientTestFactory {

    public static WebClient createWebClient(FitTrackerProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .apiVersionInserter(ApiVersionInserter.builder().usePathSegment(1).build())
                .defaultApiVersion(properties.version())
                .build();
    }

}
