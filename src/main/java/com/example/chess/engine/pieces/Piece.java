package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Piece {
    private Color color;
    private int coordX;
    private int coordY;

    public abstract boolean isLegalMove(int endX, int endY, Board board);

    public abstract boolean isLegalCapture(Piece targetPiece, Board board);

    public abstract void moveDone(int endX, int endY);
}
