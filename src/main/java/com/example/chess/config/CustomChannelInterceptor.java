package com.example.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CustomChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            logger.debug("Обработка STOMP-сообщения: command={}, session={}", accessor.getCommand(), accessor.getSessionId());
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String userHeader = accessor.getFirstNativeHeader("user");
                logger.info("Аутентификация WebSocket: userHeader={}", userHeader);
                if (userHeader != null) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(userHeader, null, null);
                    accessor.setUser(auth);
                    logger.info("Установлен пользователь для сессии {}: {}", accessor.getSessionId(), userHeader);
                } else {
                    logger.warn("Заголовок user отсутствует в WebSocket-соединении");
                }
            }
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                logger.info("Подписка на маршрут: {}, user={}", accessor.getDestination(), accessor.getUser());
            }
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                logger.info("Отправка сообщения на маршрут: {}, user={}", accessor.getDestination(), accessor.getUser());
            }
            if (StompCommand.MESSAGE.equals(accessor.getCommand())) {
                logger.info("Передача сообщения на маршрут: {}, user={}", accessor.getDestination(), accessor.getUser());
            }
        }
        return message;
    }
}