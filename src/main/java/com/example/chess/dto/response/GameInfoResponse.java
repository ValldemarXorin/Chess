package com.example.chess.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GameInfoResponse {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private PlayerResponse whitePlayer;
    private PlayerResponse blackPlayer;
    private String status;
    private String notes;

    public GameInfoResponse(long id, LocalDateTime startTime,
                            LocalDateTime endTime, String status, String notes,
                            PlayerResponse whitePlayer, PlayerResponse blackPlayer) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }
}
