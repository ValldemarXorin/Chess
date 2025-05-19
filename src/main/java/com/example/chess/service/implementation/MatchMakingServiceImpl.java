package com.example.chess.service.implementation;

import com.example.chess.dto.response.MatchFoundResponse;
import com.example.chess.entity.Player;
import com.example.chess.repository.PlayerRepository;
import com.example.chess.service.GameManagerService;
import com.example.chess.service.MatchMakingService;
import com.example.chess.service.PlayerService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MatchMakingServiceImpl implements MatchMakingService {
    private final GameManagerService gameManagerService;
    private final PlayerService playerService;
    private final SimpMessagingTemplate messagingTemplate;

    private final ConcurrentMap<Long, PlayerQueueEntry> waitingPlayers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Long> lastPingTimes = new ConcurrentHashMap<>();

    private static final long PING_TIMEOUT = 30000; // 30 секунд
    private static final long MATCHMAKING_INTERVAL = 20000; // 20 секунд
    private final PlayerRepository playerRepository;

    private final Logger logger = LoggerFactory.getLogger(MatchMakingServiceImpl.class);
    private final SimpUserRegistry userRegistry;

    @Getter
    @AllArgsConstructor
    private static class PlayerQueueEntry {
        private final Long playerId;
        private final long joinTime = System.currentTimeMillis();
    }

    @Scheduled(fixedRate = MATCHMAKING_INTERVAL)
    @Transactional
    public void processMatchmaking() {
        logger.info("Matchmaking started");
        cleanupInactivePlayers();
        logger.info("Чистка пользователей прошла успешно");

        List<Long> activePlayerIds = getActivePlayers();

        while (activePlayerIds.size() >= 2) {
            Long player1Id = activePlayerIds.remove(0);
            Long player2Id = activePlayerIds.remove(0);
            logger.info("Взяли двух игроков для создания матча.");

            createMatch(player1Id, player2Id);
            logger.info("Создание матча.");
        }
    }

    public void addPlayerToQueue(Long playerId) {
        messagingTemplate.convertAndSend("/queue/matchmaking", playerId);

        logger.info("Добавление пользователя в очередь игроков ММ");
        waitingPlayers.put(playerId, new PlayerQueueEntry(playerId));
        logger.info("Добавлен игрок в очередь.");
        updatePlayerActivity(playerId);
    }

    public void removePlayerFromQueue(Long playerId) {
        waitingPlayers.remove(playerId);
        lastPingTimes.remove(playerId);
    }

    public void updatePlayerActivity(Long playerId) {
        lastPingTimes.put(playerId, System.currentTimeMillis());
    }

    private void cleanupInactivePlayers() {
        long currentTime = System.currentTimeMillis();

        logger.info("Удаление инактивных игроков с ключами" + waitingPlayers.keySet());
        waitingPlayers.keySet().removeIf(playerId ->
                !isPlayerActive(playerId, currentTime)
        );
        logger.info("Удаление инактивных игроков завершено. Оставшиеся ключи"
                + waitingPlayers.keySet());
    }

    private boolean isPlayerActive(Long playerId, long currentTime) {
        Long lastPing = lastPingTimes.get(playerId);
        return lastPing != null && (currentTime - lastPing) < PING_TIMEOUT;
    }

    private List<Long> getActivePlayers() {
        long currentTime = System.currentTimeMillis();
        return waitingPlayers.keySet().stream()
                .filter(playerId -> isPlayerActive(playerId, currentTime))
                .collect(Collectors.toList());
    }

    @Transactional
    public void createMatch(Long player1Id, Long player2Id) {
        Player player1 = playerRepository.getById(player1Id);
        Player player2 = playerRepository.getById(player2Id);

        Long gameId = gameManagerService.createGame(player1, player2);

        notifyPlayers(player1Id, player2Id, gameId);

        waitingPlayers.remove(player1Id);
        waitingPlayers.remove(player2Id);
        lastPingTimes.remove(player1Id);
        lastPingTimes.remove(player2Id);
    }

    private void notifyPlayers(Long whitePlayerId, Long blackPlayerId, Long gameId) {

        MatchFoundResponse whiteResponse = new MatchFoundResponse(gameId, "white");
        MatchFoundResponse blackResponse = new MatchFoundResponse(gameId, "black");

        logger.info("Проверка SimpUserRegistry перед отправкой...");
        SimpUser user1 = userRegistry.getUser(whitePlayerId.toString());
        SimpUser user2 = userRegistry.getUser(blackPlayerId.toString());
        logger.info("Пользователь {}: {}", whitePlayerId, user1 != null ? "найден, сессии: "
                + user1.getSessions() : "не найден");
        logger.info("Пользователь {}: {}", blackPlayerId, user2 != null ? "найден, сессии: "
                + user2.getSessions() : "не найден");

        try {
            logger.info("Отправка уведомления игроку {} на /user/{}/queue/matchmaking: {}",
                    whitePlayerId, whitePlayerId, whiteResponse);
            messagingTemplate.convertAndSendToUser(
                    whitePlayerId.toString(),
                    "/queue/matchmaking",
                    whiteResponse
            );
            logger.info("Уведомление успешно отправлено игроку {}", whitePlayerId);
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления игроку {}: {}",
                    whitePlayerId, e.getMessage(), e);
        }

        try {
            logger.info("Отправка уведомления игроку {} на /user/{}/queue/matchmaking: {}",
                    blackPlayerId, blackPlayerId, blackResponse);
            messagingTemplate.convertAndSendToUser(
                    blackPlayerId.toString(),
                    "/queue/matchmaking",
                    blackResponse
            );
            logger.info("Уведомление успешно отправлено игроку {}", blackPlayerId);
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления игроку {}: {}",
                    blackPlayerId, e.getMessage(), e);
        }

        // Отладка: отправка на общий маршрут
        messagingTemplate.convertAndSend("/queue/matchmaking", whiteResponse);
        messagingTemplate.convertAndSend("/queue/matchmaking", blackResponse);
    }
}