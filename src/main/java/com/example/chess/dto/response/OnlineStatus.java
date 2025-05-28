package com.example.chess.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class OnlineStatus {
    private Long playerId;
    private String status;
}