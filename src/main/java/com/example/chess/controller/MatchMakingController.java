package com.example.chess.controller;

import com.example.chess.service.MatchMakingService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class MatchMakingController {

    private final MatchMakingService matchMakingService;
    private final Logger logger = LoggerFactory.getLogger(MatchMakingController.class);

    @MessageMapping("{playerId}/game/add-in-game-pool")
    public void addInGamePool(@DestinationVariable long playerId) {
        logger.info("Начало добавления пользователя в пул пользователей.");
        matchMakingService.addPlayerToQueue(playerId);
    }
}
