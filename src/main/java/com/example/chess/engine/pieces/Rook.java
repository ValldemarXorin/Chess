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
        if (endX < 0 || endX >= 8 || endY < 0 || endY >= 8
                || this.getCoordX() == endX && this.getCoordY() == endY) {
            return false;
        }

        // Проверка горизонтального движения
        if (this.getCoordX() != endX && this.getCoordY() == endY) {
            int directionX = endX - this.getCoordX() > 0 ? 1 : -1;
            for (int i = this.getCoordX() + directionX; i != endX + directionX; i += directionX) {
                if (board.getPieceAt(i, this.getCoordY()) != null) {
                    return false;  // Есть препятствие
                }
            }
            return true;
        }

        // Проверка вертикального движения
        if (this.getCoordX() == endX && this.getCoordY() != endY) {
            int directionY = endY - this.getCoordY() > 0 ? 1 : -1;
            for (int j = this.getCoordY() + directionY; j != endY + directionY; j += directionY) {
                if (board.getPieceAt(this.getCoordX(), j) != null) {
                    return false;  // Есть препятствие
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {
        if (targetPiece == null || targetPiece.getColor() == this.getColor()) {
            return false; // Не можем захватить пустую клетку или свою фигуру
        }

        int directionX = targetPiece.getCoordX() - this.getCoordX() > 0 ? 1 : -1;
        int directionY = targetPiece.getCoordY() - this.getCoordY() > 0 ? 1 : -1;
        if (isLegalMove(targetPiece.getCoordX() - directionX, targetPiece.getCoordY(), board)
                || isLegalMove(targetPiece.getCoordX(),
                targetPiece.getCoordY() - directionY, board)) {
            return true;
        }
        return false;
    }

    @Override
    public void moveDone(int endX, int endY) {
        this.setCoordX(endX);
        this.setCoordY(endY);
    }
}
