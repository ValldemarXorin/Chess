package com.example.chess.engine;

import com.example.chess.engine.pieces.*;
import com.example.chess.exception.gameException.IllegalMove;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Board {
    private Piece[][] field;
    private boolean isWhiteToMove;

    public Board() {
        field = new Piece[8][8];
        isWhiteToMove = true;
        initializeBoard();
    }

    public Piece getPieceAt(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }
        return field[x][y];
    }

    public void setPieceAt(int x, int y, Piece piece) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }
        field[x][y] = piece;
    }

    public void movePiece(int x, int y, Piece piece) throws IllegalMove {
        // Проверка допустимости хода
        Move pieceMove = new Move(piece, piece.getCoordX(), piece.getCoordY(), x, y);
        Allocation pieceAllocation = new Allocation(piece);
        List<Move> legalMoves = pieceAllocation.calculateAllMoves(this);

        if (!legalMoves.contains(pieceMove)) {
            throw new IllegalMove();
        }

        // Проверка связки
        if (GameAnalyzer.isPiecePinned(piece, this)) {
            throw new IllegalMove();
        }

        // Проверка захвата или обычного хода
        Piece targetPiece = getPieceAt(x, y);
        if (targetPiece != null) {
            if (!piece.isLegalCapture(targetPiece, this)) {
                throw new IllegalMove();
            }
        } else {
            if (!piece.isLegalMove(x, y, this)) {
                throw new IllegalMove();
            }
        }

        // Выполнение хода
        setPieceAt(piece.getCoordX(), piece.getCoordY(), null);
        setPieceAt(x, y, piece);
        piece.setCoordX(x);
        piece.setCoordY(y);
    }

    private void initializeBoard() {
        // Расстановка белых фигур
        field[0][0] = new Rook(Color.WHITE, 0, 0);   // Ладья
        field[1][0] = new Horse(Color.WHITE, 1, 0); // Конь
        field[2][0] = new Bishop(Color.WHITE, 2, 0); // Слон
        field[3][0] = new Queen(Color.WHITE, 3, 0);  // Ферзь
        field[4][0] = new King(Color.WHITE, 4, 0);   // Король
        field[5][0] = new Bishop(Color.WHITE, 5, 0); // Слон
        field[6][0] = new Horse(Color.WHITE, 6, 0); // Конь
        field[7][0] = new Rook(Color.WHITE, 7, 0);   // Ладья

        // Расстановка белых пешек
        for (int i = 0; i < 8; i++) {
            field[i][1] = new Pawn(Color.WHITE, i, 1);
        }

        // Расстановка черных фигур
        field[0][7] = new Rook(Color.BLACK, 0, 7);   // Ладья
        field[1][7] = new Horse(Color.BLACK, 1, 7); // Конь
        field[2][7] = new Bishop(Color.BLACK, 2, 7); // Слон
        field[3][7] = new Queen(Color.BLACK, 3, 7);  // Ферзь
        field[4][7] = new King(Color.BLACK, 4, 7);   // Король
        field[5][7] = new Bishop(Color.BLACK, 5, 7); // Слон
        field[6][7] = new Horse(Color.BLACK, 6, 7); // Конь
        field[7][7] = new Rook(Color.BLACK, 7, 7);   // Ладья

        // Расстановка черных пешек
        for (int i = 0; i < 8; i++) {
            field[i][6] = new Pawn(Color.BLACK, i, 6);
        }
    }

    public void changeMove() {
        isWhiteToMove = !isWhiteToMove;
    }

    public Board clone() {
        Board clonedBoard = new Board(); // Создаем новую доску

        // Копируем состояние isWhiteToMove
        clonedBoard.setWhiteToMove(this.isWhiteToMove);

        // Глубокое копирование массива field
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = this.field[x][y];
                if (piece != null) {
                    // Клонируем каждую фигуру и помещаем ее в новую доску
                    clonedBoard.setPieceAt(x, y, piece.clone());
                } else {
                    // Если клетка пуста, оставляем ее пустой
                    clonedBoard.setPieceAt(x, y, null);
                }
            }
        }

        return clonedBoard;
    }
}
