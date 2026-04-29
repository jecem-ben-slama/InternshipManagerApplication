package com.iit.internship_manager.infrastucture.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Destination for clients to subscribe to
        config.enableSimpleBroker("/topic");
        // Prefix for clients to send messages to
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Connection URL for Flutter: http://your-ip:8080/ws-endpoint
        registry.addEndpoint("/ws-endpoint")
                .setAllowedOriginPatterns("*")
              .withSockJS();
    }
}