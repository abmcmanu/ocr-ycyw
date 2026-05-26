package com.ycyw.chat.chat_poc_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket natif (STOMP) — sans SockJS pour éviter les listeners `unload` dépréciés côté client
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Les clients écoutent sur les topics préfixés par /topic (broadcast) ou /queue (unicast)
        registry.enableSimpleBroker("/topic", "/queue");
        // Les clients envoient des messages vers les endpoints préfixés par /app
        registry.setApplicationDestinationPrefixes("/app");
    }
}
