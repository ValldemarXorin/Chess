package com.example.chess.repository;

import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByName(String name);

    Optional<Player> findByEmail(String email);

    List<Player> findByNameContainingIgnoreCase(String name);

    @Query(value = "SELECT friend FROM Player p JOIN p.friends friend"
            + " WHERE p.id = :id")
    Set<Player> findAllFriends(@Param("id") Long id);

    @Query(value = "SELECT friend FROM Player p JOIN p.friends friend"
            + " WHERE friend.name = :friendName AND p.id = :id")
    Set<Player> findFriendsByPlayerName(@Param("friendName") String friendName,
                                        @Param("id") Long id);

    @Query(value = "SELECT gameInfo FROM Player p JOIN p.gamesAsWhitePlayer gameInfo"
            + " WHERE p.id = :id")
    List<GameInfo> findAllGamesInfoAsWhitePlayer(@Param("id") Long id);

    @Query(value = "SELECT gameInfo FROM Player p JOIN p.gamesAsBlackPlayer gameInfo"
            + " WHERE p.id = :id")
    List<GameInfo> findAllGamesInfoAsBlackPlayer(@Param("id") Long id);

    @Query(value = "SELECT friend FROM Player p JOIN p.friendRequests friend"
            + " WHERE p.id = :id")
    Set<Player> findAllFriendsRequests(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM friend_requests "
            + "WHERE sender_user_id = :playerId OR recipient_user_id = :playerId",
            nativeQuery = true)
    void deleteFriendRequestsByPlayerId(long playerId);

    // Удалить ВСЕ дружеские связи, где игрок участвовал
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_friends "
            + "WHERE user_id = :playerId OR friend_id = :playerId", nativeQuery = true)
    void deleteFriendshipsByPlayerId(long playerId);

    // 1. JPQL: фильтр по статусу
    @Query("SELECT DISTINCT p FROM Player p " +
            "LEFT JOIN p.gamesAsWhitePlayer whiteGames " +
            "LEFT JOIN p.gamesAsBlackPlayer blackGames " +
            "WHERE (:status IS NULL OR whiteGames.status = :status OR blackGames.status = :status)")
    List<Player> findPlayersByGameStatus(@Param("status") String status);

    // 2. Native SQL: фильтр по endTime (BETWEEN)
    @Query(value = """
        SELECT DISTINCT p.* FROM players p
        LEFT JOIN games_info whiteGames ON p.id = whiteGames.whitePlayer_id
        LEFT JOIN games_info blackGames ON p.id = blackGames.blackPlayer_id
        WHERE (:endTimeFrom IS NULL OR whiteGames.end_time >= :endTimeFrom OR blackGames.end_time >= :endTimeFrom)
        AND (:endTimeTo IS NULL OR whiteGames.end_time <= :endTimeTo OR blackGames.end_time <= :endTimeTo)
        """, nativeQuery = true)
    List<Player> findPlayersByGameEndTimeRange(
            @Param("endTimeFrom") LocalDateTime endTimeFrom,
            @Param("endTimeTo") LocalDateTime endTimeTo
    );

    // 3. Комбинированный запрос (опционально)
    @Query("SELECT DISTINCT p FROM Player p " +
            "LEFT JOIN p.gamesAsWhitePlayer whiteGames " +
            "LEFT JOIN p.gamesAsBlackPlayer blackGames " +
            "WHERE (:status IS NULL OR whiteGames.status = :status OR blackGames.status = :status) " +
            "AND (:endTimeFrom IS NULL OR whiteGames.endTime >= :endTimeFrom OR blackGames.endTime >= :endTimeFrom) " +
            "AND (:endTimeTo IS NULL OR whiteGames.endTime <= :endTimeTo OR blackGames.endTime <= :endTimeTo)")
    Page<Player> findPlayersByFilters(
            @Param("status") String status,
            @Param("endTimeFrom") LocalDateTime endTimeFrom,
            @Param("endTimeTo") LocalDateTime endTimeTo,
            Pageable pageable
    );
}
