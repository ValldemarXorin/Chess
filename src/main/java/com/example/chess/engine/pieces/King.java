package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;

//Дописать

public class King extends Piece {

    public King(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        return false;
    }
}
