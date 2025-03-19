package com.example.chess.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameInfoDtoRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;

    public GameInfoDtoRequest(LocalDateTime startTime, LocalDateTime endTime, String status, String notes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.notes = notes;
    }
}
