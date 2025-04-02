package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;
import com.example.chess.engine.GameAnalyzer;

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

        if (!(Math.abs(endX - this.getCoordX()) <= 1 && Math.abs(endY - this.getCoordY()) <= 1)) {
            return false;
        }

        return board.getPieceAt(endX, endY) == null;
    }

    @Override
    public Piece copy() {
        return new King(this.getColor(), this.getCoordX(), this.getCoordY());
    }

    public boolean isCastlingPossible(Board board) {
        return board.getPieceAt(this.getCoordX(), this.getCoordY()) == null;
    }
}
