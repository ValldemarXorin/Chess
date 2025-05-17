package com.example.chess.dto.response;

import com.example.chess.engine.pieces.Color;
import com.example.chess.engine.pieces.Piece;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameStateResponse {
    public Piece[][] board;
    public String status;
    public Color currentTurnColor;
}
