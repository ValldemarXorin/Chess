package com.example.chess.dto.response;

import com.example.chess.engine.pieces.Piece;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameStateResponse {
    public final Piece[][] board;
    public final String status;
    public final String currentTurnColor;
}
