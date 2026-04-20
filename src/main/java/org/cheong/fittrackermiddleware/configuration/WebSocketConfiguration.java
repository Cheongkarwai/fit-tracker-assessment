package org.cheong.fittrackermiddleware.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

/**
 * @author cheong
 * Configuration for WebSocketClient
 */
@Configuration
public class WebSocketConfiguration {

    /**
     * WebSocketClient for WebSocket
     * @return WebSocketClient
     */
    @Bean
    public WebSocketClient webSocketClient() {
        return new ReactorNettyWebSocketClient();
    }
}
