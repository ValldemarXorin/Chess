package com.example.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomChannelInterceptor customChannelInterceptor;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    public WebSocketConfig(CustomChannelInterceptor customChannelInterceptor) {
        this.customChannelInterceptor = customChannelInterceptor;
        logger.info("CustomChannelInterceptor инициализирован: {}", customChannelInterceptor);
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chess")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(15 * 1000)          // (1) Лимит отправки сообщений
                .setSendBufferSizeLimit(512 * 1024)    // (2) Буфер отправки
                .setTimeToFirstMessage(30 * 1000);     // (3) Таймаут подключения
    }
}
