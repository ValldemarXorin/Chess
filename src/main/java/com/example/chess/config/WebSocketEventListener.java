package com.example.chess.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.chess.service.StatusService;

@Component
public class WebSocketEventListener {

    private final StatusService statusService;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    public WebSocketEventListener(StatusService statusService) {
        this.statusService = statusService;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String playerId = (String) accessor.getSessionAttributes().get("playerId");
        String sessionId = accessor.getSessionId();

        if (playerId != null) {
            try {
                statusService.updateUserStatus(Long.parseLong(playerId), "ONLINE");
                logger.info("WebSocket подключен для игрока: {}, сессия: {}", playerId, sessionId);
            } catch (NumberFormatException e) {
                logger.error("Невалидный playerId: {} для сессии {}", playerId, sessionId);
            }
        } else {
            logger.warn("WebSocket подключен без playerId, сессия: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String playerId = (String) accessor.getSessionAttributes().get("playerId");
        String sessionId = accessor.getSessionId();

        if (playerId != null) {
            try {
                statusService.updateUserStatus(Long.parseLong(playerId), "OFFLINE");
                logger.info("WebSocket отключен для игрока: {}, сессия: {}", playerId, sessionId);
            } catch (NumberFormatException e) {
                logger.error("Невалидный playerId: {} для сессии {}", playerId, sessionId);
            }
        } else {
            logger.warn("WebSocket отключен без playerId, сессия: {}", sessionId);
        }
    }
}