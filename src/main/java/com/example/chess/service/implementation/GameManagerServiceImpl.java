package com.example.chess.service.implementation;

import com.example.chess.entity.Player;
import com.example.chess.repository.GameInfoRepository;
import com.example.chess.service.GameManagerService;
import com.example.chess.service.GameService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class GameManagerServiceImpl implements GameManagerService {

    private final Map<Long, GameService> activeGames = new ConcurrentHashMap<>();
    private final GameInfoRepository gameInfoRepository;
    private final ApplicationContext applicationContext;

    private final Logger logger = LoggerFactory.getLogger(GameManagerServiceImpl.class);

    public GameManagerServiceImpl(GameInfoRepository gameInfoRepository,
                                  ApplicationContext applicationContext) {
        this.gameInfoRepository = gameInfoRepository;
        this.applicationContext = applicationContext;
    }

    public Long createGame(Player whitePlayer, Player blackPlayer) {
        logger.info("Начинаем создавать игру");
        GameService gameService = applicationContext.getBean(GameService.class);
        long gameId = gameService.initGame(whitePlayer, blackPlayer);
        logger.info("Создали игру с id" + gameId);
        activeGames.put(gameId, gameService);
        return gameId;
    }

    public GameService getActiveGame(long gameId) {
        return activeGames.containsKey(gameId) ? activeGames.get(gameId) : null;
    }
}
