package org.cheong.fittrackermiddleware.adapter;

import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class FitTrackerWebSocketAdapter {

    private final WebSocketHelper webSocketHelper;
    private final FitTrackerProperties properties;

    public FitTrackerWebSocketAdapter(WebSocketHelper webSocketHelper, FitTrackerProperties properties) {
        this.webSocketHelper = webSocketHelper;
        this.properties = properties;
    }

    public Flux<String> getTraces() {
        return webSocketHelper.receive(properties.getWebSocketBaseUrl().resolve("/api/v1/traces"));
    }
}
