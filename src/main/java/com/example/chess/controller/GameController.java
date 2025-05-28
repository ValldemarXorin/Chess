package com.example.chess.controller;

import com.example.chess.dto.request.ChessMoveRequest;
import com.example.chess.dto.response.GameStateResponse;
import com.example.chess.engine.pieces.Piece;
import com.example.chess.exception.game.IllegalMove;
import com.example.chess.service.GameManagerService;
import com.example.chess.service.GameService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class GameController {

    private GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameManagerService gameManagerService;

    @MessageMapping("/game/{gameId}/move")
    public void move(@DestinationVariable String gameId, @Payload ChessMoveRequest move)
            throws IllegalMove {
        this.gameService = this.gameManagerService.getActiveGame(Long.parseLong(gameId));
        if (move.getPlayerId() == gameService.getGameInfo().getWhitePlayer().getId()
                && gameService.getBoard().isWhiteToMove()
                || move.getPlayerId() == gameService.getGameInfo().getBlackPlayer().getId()
                && !gameService.getBoard().isWhiteToMove()) {
            this.gameService.makeMove(move.getStartX(), move.getStartY(),
                    move.getEndX(), move.getEndY());
            GameStateResponse gameStateResponse = new GameStateResponse();
            gameStateResponse.setBoard(gameService.showBoard());
            String status = "in_process";
            gameStateResponse.setStatus(status);
            gameStateResponse.setCurrentTurnColor(move.getPlayerId() == gameService.getGameInfo().getWhitePlayer().getId() ? "black" : "white");
            this.messagingTemplate.convertAndSend("/game/" + gameId + "/move", gameStateResponse);
        }
    }
}
