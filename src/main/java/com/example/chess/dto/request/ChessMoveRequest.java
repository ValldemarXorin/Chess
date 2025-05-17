package com.example.chess.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessMoveRequest {
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private long playerId;
}
