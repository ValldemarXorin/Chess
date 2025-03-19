package com.example.chess.engine;

import com.example.chess.engine.pieces.Piece;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Move {
    private Piece piece;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public Move(Piece piece, int startX, int startY, int endX, int endY) {
        this.piece = piece;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }
}
