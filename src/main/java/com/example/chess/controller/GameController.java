package com.example.chess.controller;

import com.example.chess.dto.request.ChessMoveRequest;
import com.example.chess.exception.game.IllegalMove;
import com.example.chess.service.GameManagerService;
import com.example.chess.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private GameManagerService gameManagerService;

    @MessageMapping("/game/{gameId}/move")
    public void move(@DestinationVariable String gameId, @Payload ChessMoveRequest move) throws IllegalMove {
        this.gameService = this.gameManagerService.getActiveGame(Long.parseLong(gameId));
        if (move.getPlayerId() == gameService.getGameInfo().getWhitePlayer().getId()
                && gameService.getBoard().isWhiteToMove()
                || move.getPlayerId() == gameService.getGameInfo().getBlackPlayer().getId()
                && !gameService.getBoard().isWhiteToMove()) {
            this.gameService.makeMove(move.getStartX(), move.getStartY(),
                    move.getEndX(), move.getEndY());
        }
        this.messagingTemplate.convertAndSend("/game/" + gameId + "/move", move);
    }
}
