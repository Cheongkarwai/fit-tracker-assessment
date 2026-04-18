package org.cheong.fittrackermiddleware.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "fit-tracker")
public record FitTrackerProperties(String baseUrl,
                                  String clientId,
                                  String clientSecret,
                                  String version) {

    public URI getWebSocketBaseUrl() {
        return URI.create(baseUrl.replaceFirst("^https", "wss"));
    }
}
