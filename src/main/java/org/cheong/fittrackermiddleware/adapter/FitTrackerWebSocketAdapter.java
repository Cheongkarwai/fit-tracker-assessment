package org.cheong.fittrackermiddleware.adapter;

import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author cheong
 * Adapter for Fit Tracker WebSocket API
 */
@Component
public class FitTrackerWebSocketAdapter {

    private final WebSocketHelper webSocketHelper;
    private final FitTrackerProperties properties;

    /**
     * Constructor for FitTrackerWebSocketAdapter
     * @param webSocketHelper websocket helper
     * @param properties properties for Fit Tracker server
     */
    public FitTrackerWebSocketAdapter(WebSocketHelper webSocketHelper, FitTrackerProperties properties) {
        this.webSocketHelper = webSocketHelper;
        this.properties = properties;
    }

    /**
     * Get traces from Fit Tracker server
     * @return Flux<String>
     */
    public Flux<String> getTraces() {
        return webSocketHelper.receive(properties.getWebSocketBaseUrl().resolve("/api/v1/traces"));
    }
}
