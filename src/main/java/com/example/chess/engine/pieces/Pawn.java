package com.example.chess.engine.pieces;

import com.example.chess.engine.Board;
import lombok.Getter;
import lombok.Setter;


// попытаться подогнать под общую структуру фигур

@Getter
@Setter
public class Pawn extends Piece {
    boolean isFirstMove = true;
    boolean isEnPassant = false;
    int direction;

    public Pawn(Color color, int x, int y) {
        this.setColor(color);
        this.setCoordX(x);
        this.setCoordY(y);
        direction = color.equals(Color.WHITE) ? 1 : -1;
    }

    @Override
    public boolean isLegalMove(int endX, int endY, Board board) {
        if (isInvalidPosition(endX, endY)) {
            return false;
        }

        if (this.getCoordX() != endX) {
            return false;
        }
        if (isFirstMove && this.getCoordY() + 2 * direction == endY
                && board.getPieceAt(endX, endY) == null
                && board.getPieceAt(endX, endY - 1 * direction) == null) {
            return true;
        }
        return this.getCoordY() + 1 * direction == endY && board.getPieceAt(endX, endY) == null;
    }

    @Override
    public boolean isLegalCapture(Piece targetPiece, Board board) {

        if (targetPiece == null || targetPiece.getColor() == this.getColor()) {
            return false; // Не можем захватить пустую клетку или свою фигуру
        }

        // взятие
        if ((this.getCoordX() + 1 == targetPiece.getCoordX()
                || this.getCoordX() - 1 == targetPiece.getCoordX())
                && this.getCoordY() + 1 * direction == targetPiece.getCoordY()
                && targetPiece.getColor() != this.getColor()) {
            return true;
        }

        // взятие на проходе
        return targetPiece instanceof Pawn pawn
                && pawn.isEnPassant()
                && board.getPieceAt(targetPiece.getCoordX(), targetPiece.getCoordY()) == null;
    }

    @Override
    public void moveDone(int endX, int endY) {
        if (isEnPassant) {
            isEnPassant = false;  // Сброс состояния взятия на проходе
        }
        if (isFirstMove) {
            if (this.getCoordY() + 2 * direction == endY) {
                isEnPassant = true;  // Установка состояния для взятия на проходе
            }
            isFirstMove = false;  // Обновление состояния первого хода
        }
        this.setCoordX(endX);
        this.setCoordY(endY);
    }

}
