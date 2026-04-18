package org.cheong.fittrackermiddleware.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fit-tracker")
public record FitTrackerProperties(String baseUrl,
                                  String clientId,
                                  String clientSecret,
                                  String version) {
}
