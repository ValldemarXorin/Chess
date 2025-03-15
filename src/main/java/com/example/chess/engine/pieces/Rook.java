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
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        int directionX = endX - this.getCoordX() > 0 ? 1 : -1;
        int directionY = endY - this.getCoordY() > 0 ? 1 : -1;


    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {

    }
}
