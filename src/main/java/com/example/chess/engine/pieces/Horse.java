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
        if (endX < 0 || endX >= 8 || endY < 0 || endY >= 8
                || this.getCoordX() == endX && this.getCoordY() == endY) {
            return false;
        }

        int directionX = endX - this.getCoordX();
        int directionY = endY - this.getCoordY();

        return board.getPieceAt(endX, endY) == null &&
                ((Math.abs(directionX) == 2 && Math.abs(directionY) == 1) ||
                        (Math.abs(directionX) == 1 && Math.abs(directionY) == 2));
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        if (targetPiece == null || targetPiece.getColor() == this.getColor()) {
            return false; // Не можем захватить пустую клетку или свою фигуру
        }

        int directionX = targetPiece.getCoordX() - this.getCoordX();
        int directionY = targetPiece.getCoordY() - this.getCoordY();

        return Math.abs(directionX) == 2 && Math.abs(directionY) == 1 ||
                Math.abs(directionX) == 1 && Math.abs(directionY) == 2;
    }

    @Override
    public void moveDone(int endX, int endY) {
        this.setCoordX(endX);
        this.setCoordY(endY);
    }
}
