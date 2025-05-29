package com.example.chess.controller;

import com.example.chess.dto.request.ChessMoveRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;

@Controller
public class GameController {

    // Принимаем сообщение о ходе от клиента и перенаправляем его на топик игры
    @MessageMapping("/game/{gameId}/move")
    @SendTo("/topic/game/{gameId}/move")
    public ChessMoveRequest forwardMove(@DestinationVariable String gameId, @Payload ChessMoveRequest move) {
        // Логирование для отладки
        System.out.println("Received move for game " + gameId + ": " + move);
        // Просто возвращаем полученный ход для рассылки всем подписчикам
        return move;
    }
}
