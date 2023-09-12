package fr.metamorpion.api.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/room");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Create a websocket endpoint mapped to /data-info
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Socket endpoint
        registry.addEndpoint("/data-info")
                .withSockJS();
    }

}
