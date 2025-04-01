package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;

public class Horse extends Piece {

    public Horse(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        int distanceX = endX - this.getCoordX();
        int distanceY = endY - this.getCoordY();

        return board.getPieceAt(endX, endY) == null
                && ((Math.abs(distanceX) == 2 && Math.abs(distanceY) == 1)
                || (Math.abs(distanceX) == 1 && Math.abs(distanceY) == 2));
    }

    @Override
    public Piece copy() {
        return new Horse(this.getColor(), this.getCoordX(), this.getCoordY());
    }
}
