package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Piece implements Cloneable {
    private Color color;
    private int coordX;
    private int coordY;

    public abstract boolean isLegalMove(int endX, int endY, Board board);

    public abstract boolean isLegalCapture(Piece targetPiece, Board board);

    public void moveDone(int endX, int endY) {
        this.setCoordX(endX);
        this.setCoordY(endY);
    }

    protected boolean isInvalidPosition(int endX, int endY) {
        if (endX < 0 || endX >= 8 || endY < 0 || endY >= 8
                || this.getCoordX() == endX && this.getCoordY() == endY) {
            return true;
        }
        return false;
    }

    @Override
    public Piece clone() {
        try {
            return (Piece) super.clone(); // Используем стандартный механизм клонирования
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning not supported", e);
        }
    }
}
