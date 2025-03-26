package com.example.chess.repository;

import com.example.chess.entity.GameInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameInfoRepository extends JpaRepository<GameInfo, Long> {
    List<GameInfo> findByStatus(String status);
}
