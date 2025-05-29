package com.example.chess.controller;

import com.example.chess.dto.request.ChessMoveRequest;
import com.example.chess.dto.response.GameStateResponse;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.exception.game.IllegalMove;
import com.example.chess.service.GameManagerService;
import com.example.chess.service.GameService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.chess.dto.request.ChessMoveRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

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
