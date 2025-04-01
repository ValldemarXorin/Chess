package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;

public class Bishop extends Piece {

    public Bishop(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        if (Math.abs(endX - this.getCoordX()) != Math.abs(endY - this.getCoordY())) {
            return false;
        }

        int directionX = endX - this.getCoordX() > 0 ? 1 : -1;
        int directionY = endY - this.getCoordY() > 0 ? 1 : -1;

        for (int i = this.getCoordX() + directionX, j = this.getCoordY() + directionY;
                    i != endX && j != endY; i += directionX, j += directionY) {
            if (board.getPieceAt(i, j) != null) {
                return false;
            }
        }

        return true;
    }

}
