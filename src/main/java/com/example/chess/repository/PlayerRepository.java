package com.example.chess.repository;

import com.example.chess.entity.Player;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findById(long id);

    List<Player> findByName(String name);

    Optional<Player> findByEmail(String email);
}
