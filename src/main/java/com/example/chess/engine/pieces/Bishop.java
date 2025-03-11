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
        if (endX < 0 || endX >= 8 || endY < 0 || endY >= 8
                || this.getCoordX() == endX && this.getCoordY() == endY) {
            return false;
        }

        if (Math.abs(endX - this.getCoordX()) == Math.abs(endY - this.getCoordY())) {
            int directionXD = endX - this.getCoordX() > 0 ? 1 : -1;
            int directionYD = endY - this.getCoordY() > 0 ? 1 : -1;

            for (int i = this.getCoordX() + directionXD, j = this.getCoordY() + directionYD;
                 i != endX + directionXD; i += directionXD, j += directionYD) {
                if (board.getPieceAt(i, j) != null) {
                    return false;
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

        int directionXD = targetPiece.getCoordX() - this.getCoordX() > 0 ? 1 : -1;
        int directionYD = targetPiece.getCoordY() - this.getCoordY() > 0 ? 1 : -1;

        if (isLegalMove(targetPiece.getCoordX() - directionXD, targetPiece.getCoordY() - directionYD, board)) {
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
