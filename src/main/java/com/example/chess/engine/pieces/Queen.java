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
        return endX == this.getCoordX() && endY == this.getCoordY();
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        return targetPiece.getCoordX() == this.getCoordX()
                && targetPiece.getCoordY() == this.getCoordY();
    }
}
