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

    @Query(value = "SELECT friend FROM Player p JOIN p.friends friend WHERE p.id = :id")
    Set<Player> findAllFriends(@Param("id") Long id);

    @Query(value = "SELECT friend FROM Player p JOIN p.friends friend WHERE friend.name = :friendName AND p.id = :id")
    Set<Player> findFriendsByPlayerName(@Param("friendName") String friendName, @Param("id") Long id);

    @Query(value = "SELECT gameInfo FROM Player p JOIN p.gamesAsWhitePlayer gameInfo WHERE p.id = :id")
    List<GameInfo> findAllGamesInfoAsWhitePlayer(@Param("id") Long id);

    @Query(value = "SELECT gameInfo FROM Player p JOIN p.gamesAsBlackPlayer gameInfo WHERE p.id = :id")
    List<GameInfo> findAllGamesInfoAsBlackPlayer(@Param("id") Long id);

    @Query(value = "SELECT friend FROM Player p JOIN p.friendRequests friend WHERE p.id = :id")
    Set<Player> findAllFriendsRequests(@Param("id") Long id);
}
