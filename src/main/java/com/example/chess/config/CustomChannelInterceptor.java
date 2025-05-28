package com.example.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CustomChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CustomChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        logger.info("StompHeaderAccessor: {}, getCommand() {}", accessor, accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String user = accessor.getFirstNativeHeader("user");
            if (user != null && !user.isEmpty()) {
                accessor.setUser(new UsernamePasswordAuthenticationToken(user, null));
                accessor.getSessionAttributes().put("playerId", user); // Сохраняем playerId в sessionAttribute
                logger.info("Аутентификация WebSocket: установлен пользователь {} для сессии {}",
                        user, accessor.getSessionId());
            } else {
                logger.error("Заголовок user отсутствует в CONNECT для сессии {}",
                        accessor.getSessionId());
                // Не прерываем соединение, но пользователь не будет зарегистрирован
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            logger.info("Подписка на маршрут: {}, пользователь: {}, сессия: {}",
                    destination,
                    accessor.getUser() != null ? accessor.getUser().getName() : "null",
                    accessor.getSessionId());
        }

        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            logger.info("Отключение сессии: {}", accessor.getSessionId());
        }

        return message;
    }
}