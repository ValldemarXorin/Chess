package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;

// переписать
public class Queen extends Piece {
    public Queen(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        // For Queen's movement (horizontal, vertical, or diagonal)
        if (!(endX - this.getCoordX() == 0 && endY - this.getCoordY() != 0)  // vertical
                && !(endX - this.getCoordX() != 0 && endY - this.getCoordY() == 0)  // horizontal
                && !(Math.abs(endX - this.getCoordX())
                == Math.abs(endY - this.getCoordY()))) {
            return false;
        }

        int directionX = endX - this.getCoordX();
        if (directionX != 0) {
            directionX = directionX > 0 ? 1 : -1;
        }

        int directionY = endY - this.getCoordY();
        if (directionY != 0) {
            directionY = directionY > 0 ? 1 : -1;
        }

        for (int i = this.getCoordX() + directionX, j = this.getCoordY() + directionY;
                i <= endX && j <= endY; i += directionX, j += directionY) {
            if (board.getPieceAt(i, j) != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Piece copy() {
        return new Queen(this.getColor(), this.getCoordX(), this.getCoordY());
    }

}
