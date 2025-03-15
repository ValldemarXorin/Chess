package com.example.chess.repository;

import com.example.chess.entity.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameInfoRepository extends JpaRepository<GameInfo, Long> {
    GameInfo findById(long id);
}
