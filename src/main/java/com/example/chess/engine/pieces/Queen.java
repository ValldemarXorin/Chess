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
        if (endX == this.getCoordX() && endY == this.getCoordY()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        if (targetPiece.getCoordX() == this.getCoordX()
        && targetPiece.getCoordY() == this.getCoordY()) {
            return true;
        }

        return false;
    }
}
