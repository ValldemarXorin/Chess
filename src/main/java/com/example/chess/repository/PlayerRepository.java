package com.example.chess.repository;

import com.example.chess.entity.GameInfo;
import com.example.chess.entity.Player;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByName(String name);

    Optional<Player> findByEmail(String email);

    List<Player> findByNameContainingIgnoreCase(String name);

    @Query("SELECT friend FROM Player p JOIN p.friends friend")
    Set<Player> findAllFriends();

    @Query("SELECT friend FROM Player p JOIN p.friends friend WHERE p.name = :playerName")
    Set<Player> findFriendsByPlayerName(@Param("playerName") String playerName);

    @Query("SELECT gameInfo FROM Player p JOIN p.gamesAsWhitePlayer gameInfo")
    List<GameInfo> findAllGamesInfoAsWhitePlayer();

    @Query("SELECT gameInfo FROM Player p JOIN p.gamesAsBlackPlayer gameInfo")
    List<GameInfo> findAllGamesInfoAsBlackPlayer();
}
