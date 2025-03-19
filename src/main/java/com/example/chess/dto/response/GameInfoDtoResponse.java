package com.example.chess.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameInfoDtoResponse {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;

    public GameInfoDtoResponse(long id, LocalDateTime startTime, LocalDateTime endTime, String status, String notes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
    }
}
