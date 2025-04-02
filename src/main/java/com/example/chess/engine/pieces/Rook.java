package com.example.chess.engine.pieces;

import com.example.chess.engine.Allocation;
import com.example.chess.engine.Board;
import com.example.chess.engine.GameAnalyzer;
import lombok.Getter;
import lombok.Setter;

public class Rook extends Piece {

    @Getter @Setter
    boolean isFirstMove;

    public Rook(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
        this.isFirstMove = true;
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        return (endX - this.getCoordX() == 0 && endY - this.getCoordY() != 0)
                || (endX - this.getCoordX() != 0 && endY - this.getCoordY() == 0);
    }

    @Override
    public Piece copy() {
        return new Rook(this.getColor(), this.getCoordX(), this.getCoordY());
    }
}
