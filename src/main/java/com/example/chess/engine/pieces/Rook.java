package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;

public class Rook extends Piece {
    public Rook(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        return !isInvalidPosition(endX, endY);
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        return false;
    }
}
