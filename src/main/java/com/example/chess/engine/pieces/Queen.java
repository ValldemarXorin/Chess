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

        if (!(endX - this.getCoordX() != 0 && endY - this.getCoordY() == 0
                || endX - this.getCoordX() == 0 && endY - this.getCoordY() != 0
                || Math.abs(endX - this.getCoordX()) == Math.abs(endY - this.getCoordY()))) {
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

        for (int i = this.getCoordX() + directionX; i != endX && directionY == 0; i += directionX) {
            // движение по горизонтали
            if (board.getPieceAt(i, this.getCoordY()) != null) {
                return false;
            }
        }

        for (int j = this.getCoordY() + directionY; j != endY
                && directionX == 0; j += directionY) {
            if (board.getPieceAt(this.getCoordX(), j) != null) {
                return false;
            }
        }

        for (int i = this.getCoordX() + directionX, j = this.getCoordY() + directionY;
                i != endX && j != endY; i += directionX, j += directionY) {
            // в условии цикла добавлен j != endY. он не нужен, добавлен
            // для большей читаемости и понимания происходящего
            // выход из цикла можно отслеживать по оному из этих параметров
            // (i != endX или j != endY)
            if (board.getPieceAt(i, j) != null) {
                return false;
            }
        }

        return true;
    }

}
