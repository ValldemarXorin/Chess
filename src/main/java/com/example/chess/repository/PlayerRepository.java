package com.example.chess.repository;

import com.example.chess.entity.Player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
    Optional<Player> findByEmail(String email);
    Optional<Player> findByEmailAndName(String email, String name);

    List<Player> getAllPlayers();

    Player removeByEmail(String email);

    boolean existsByEmail(String email);
}
