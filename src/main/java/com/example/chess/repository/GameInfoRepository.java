package com.example.chess.repository;

import com.example.chess.entity.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameInfoRepository extends JpaRepository<GameInfo, Long> {
    List<GameInfo> findByStatus(String status);
}
