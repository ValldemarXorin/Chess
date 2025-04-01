package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Piece {
    private Color color;
    private int coordX;
    private int coordY;

    public abstract boolean isLegalMove(int endX, int endY, Board board);
    // структура:
    // 1. проверка на isInvalidPosition
    // 2. проверка на правильность хода фигуры (по правилам)
    // 3. определить направление движения фигуры по OY (directionY) и OX (directionX) 9
    // (-1 в отрицательном направлении, 0 нет движения по данной оси, 1 в положительном направлении)
    // 4. проверка на возможность хода фигуры (не мешает ли другая фигура)

    public boolean isLegalCapture(Piece targetPiece, Board board) {
        if (targetPiece == null) {
            return false;
        }

        Board boardCopy = new Board(board);
        boardCopy.setPieceAt(targetPiece.getCoordX(), targetPiece.getCoordY(), null);

        return isLegalMove(targetPiece.getCoordX(), targetPiece.getCoordY(), boardCopy);
    }

    public void moveDone(int endX, int endY) {
        this.setCoordX(endX);
        this.setCoordY(endY);
    }

    protected boolean isInvalidPosition(int endX, int endY) {
        return endX < 0 || endX >= 8 || endY < 0 || endY >= 8
                || (this.getCoordX() == endX && this.getCoordY() == endY);
    }

    // надо подумать над реализацией копироавния фигуры
}
