package com.example.chess.dto.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameInfoRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;
    private long whitePlayerId;
    private long blackPlayerId;

    public GameInfoRequest(LocalDateTime startTime, LocalDateTime endTime,
                           String status, String notes, long whitePlayerId,
                           long blackPlayerId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.whitePlayerId = whitePlayerId;
        this.blackPlayerId = blackPlayerId;
    }
}
