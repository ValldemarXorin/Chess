package com.example.chess.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameInfoDtoResponse {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private PlayerDtoResponse whitePlayer;
    private PlayerDtoResponse blackPlayer;
    private String status;
    private String notes;

    public GameInfoDtoResponse(long id, LocalDateTime startTime,
                               LocalDateTime endTime, String status, String notes,
                               PlayerDtoResponse whitePlayer, PlayerDtoResponse blackPlayer) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }
}
